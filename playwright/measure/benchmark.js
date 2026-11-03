// Times the API endpoints against the big data set and writes the numbers to a JSON file.
//
//   node measure/benchmark.js <label> <path-to-app-log> <output.json>
//
// The backend must be running on :8080 against the seeded throwaway database, with
//   MJCINEMA_TIMING_COUNT_QUERIES=true   (so the server log also says how many SQL statements each request ran)
// and one request at a time (this script is sequential, so the query counts are exact).
const { execFileSync } = require("node:child_process");
const fs = require("node:fs");

const API = process.env.API_URL || "http://localhost:8080";
const MYSQL = process.env.MYSQL_BIN || "C:/mysql-client/bin/mysql.exe";
const [label, logPath, outPath] = process.argv.slice(2);

const sql = (statement) =>
  execFileSync(MYSQL, ["--host=127.0.0.1", "--port=3307", "--user=root", "-N", "-B", "mjcinema_test", "-e", statement]).toString().replace(/\r/g, "").trim();

async function login(username) {
  const res = await fetch(`${API}/user/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password: "pass1234" }),
  });
  return (await res.json()).jwtToken;
}

const median = (a) => [...a].sort((x, y) => x - y)[Math.floor(a.length / 2)];
const percentile = (a, p) => [...a].sort((x, y) => x - y)[Math.min(a.length - 1, Math.floor(a.length * p))];

(async () => {
  const admin = await login("admin1");
  const bench = await login("bench");
  const auth = (t) => ({ Authorization: `Bearer ${t}` });

  // fixed, repeatable targets picked from the data
  const scheduleId = Number(sql("SELECT schedule_id FROM schedule WHERE status='ACTIVE' AND show_date > CURDATE() + INTERVAL 5 DAY ORDER BY schedule_id LIMIT 1 OFFSET 200"));
  const movieId = Number(sql(`SELECT movie_id FROM schedule WHERE schedule_id = ${scheduleId}`));
  const [oCinema, oDate, oSlot] = sql("SELECT cinema_id, show_date, slot FROM schedule WHERE status='ACTIVE' AND show_date > CURDATE() + INTERVAL 5 DAY ORDER BY schedule_id LIMIT 1 OFFSET 300").split("\t");
  const freeSeats = sql(`SELECT st.seat_number FROM seat st WHERE st.cinema_id = (SELECT cinema_id FROM schedule WHERE schedule_id = ${scheduleId}) AND st.seat_id NOT IN (SELECT seat_id FROM tickets WHERE schedule_id = ${scheduleId} AND ticket_status <> 'CANCELLED') ORDER BY st.seat_id LIMIT 60`).split("\n");
  const rows = {
    tickets: Number(sql("SELECT COUNT(*) FROM tickets")),
    schedules: Number(sql("SELECT COUNT(*) FROM schedule")),
  };
  console.log(`[${label}] schedule ${scheduleId}, movie ${movieId}, ${freeSeats.length} free seats, ${rows.tickets} tickets`);

  let bookIndex = 0;
  const get = (path, token) => ({ method: "GET", path, run: () => fetch(API + path, { headers: token ? auth(token) : {} }) });
  const endpoints = [
    { name: "Now showing", ...get("/schedule/now-showing") },
    { name: "Coming soon", ...get("/schedule/coming-soon") },
    { name: "Movie showtimes (47 showtimes)", ...get(`/schedule/movieSchedules/${movieId}`) },
    { name: "One schedule", ...get(`/schedule/${scheduleId}`) },
    { name: "Seat map", ...get(`/schedule/seats?id=${scheduleId}`) },
    { name: "Occupied seats", ...get(`/seat/occupied/${scheduleId}`) },
    { name: "Book one seat", method: "POST", path: "/ticket/book",
      run: () => fetch(API + "/ticket/book", { method: "POST", headers: { "Content-Type": "application/json", ...auth(bench) },
        body: JSON.stringify({ userId: 2, scheduleId, selectedSeat: [freeSeats[bookIndex++]] }) }) },
    { name: "Add schedule (overlap check, rejected)", method: "POST", path: "/schedule/add",
      run: () => fetch(API + "/schedule/add", { method: "POST", headers: { "Content-Type": "application/json", ...auth(admin) },
        body: JSON.stringify({ movieId: 1, cinemaId: Number(oCinema), showDate: oDate, slot: oSlot, vipPrice: 1, regPrice: 1, balPrice: 1 }) }) },
    { name: "My tickets (user with ~93)", ...get("/ticket/mytickets?userId=100", admin) },
    { name: "Seat stats (admin)", ...get("/schedule/seatStats") },
    { name: "Best showtimes (admin)", ...get("/schedule/bestShowtime") },
    { name: "Average revenue (admin)", ...get("/schedule/showAverageRevenue") },
    { name: "Movie table (admin)", ...get("/movie/showMovieTable") },
    { name: "Movie leaderboard (admin)", ...get("/movie/leaderboard") },
    { name: "Total sales (admin)", ...get("/ticket/totalSales", admin) },
    { name: "Total bookings (admin)", ...get("/ticket/showTotalBookings", admin) },
    { name: "Revenue today (admin)", ...get("/ticket/showRevenueToday", admin) },
    { name: "Users table (admin)", ...get("/user/showUsers", admin) },
    { name: "Weekly sales trend (admin)", ...get("/ticket/getSalesTrend", admin) },
    { name: "All bookings (admin)", ...get("/ticket/showBookings", admin) },
  ];

  const results = {};
  for (const ep of endpoints) {
    // two warm-up calls (they also tell us how heavy the endpoint is), then 30 / 5 / 2 measured calls
    const warm = [];
    for (let i = 0; i < 2; i++) {
      const t0 = performance.now();
      const res = await ep.run();
      await res.text();
      warm.push(performance.now() - t0);
    }
    const heaviest = Math.max(...warm);
    const n = heaviest > 5000 ? 2 : heaviest > 500 ? 5 : 30;
    const times = [];
    let status = 0;
    for (let i = 0; i < n; i++) {
      const t0 = performance.now();
      const res = await ep.run();
      await res.text();
      times.push(performance.now() - t0);
      status = res.status;
    }
    results[ep.name] = {
      method: ep.method, path: ep.path, status, runs: n,
      medianMs: +median(times).toFixed(1), p95Ms: +percentile(times, 0.95).toFixed(1), minMs: +Math.min(...times).toFixed(1),
    };
    console.log(`  ${ep.name.padEnd(42)} ${String(status).padEnd(4)} median ${results[ep.name].medianMs} ms  (n=${n})`);
  }

  // the booked test tickets must not stay in the database for the next run
  sql("DELETE FROM tickets WHERE user_id = 2");

  // server-side view: SQL statements per request, from the server log (last n lines of every endpoint)
  const lines = fs.readFileSync(logPath, "utf8").split("\n");
  const re = /TimingFilter\s*:\s*(\w+) (\S+) -> (\d+) in (\d+) ms(?:, (\d+) queries)?/;
  const seen = {};
  for (const l of lines) {
    const m = re.exec(l);
    if (m) (seen[`${m[1]} ${m[2]}`] ||= []).push({ ms: Number(m[4]), queries: m[5] === undefined ? null : Number(m[5]) });
  }
  for (const ep of endpoints) {
    const all = seen[`${ep.method} ${ep.path}`] || [];
    const mine = all.slice(-results[ep.name].runs);
    if (mine.length) {
      results[ep.name].serverMedianMs = median(mine.map((x) => x.ms));
      results[ep.name].queries = mine[mine.length - 1].queries;
    }
  }

  // the once-a-minute status refresh: wait for three samples
  const refresh = [];
  const deadline = Date.now() + 250_000;
  while (Date.now() < deadline) {
    refresh.length = 0;
    for (const l of fs.readFileSync(logPath, "utf8").split("\n")) {
      const m = /scheduled status refresh took (\d+) ms/.exec(l);
      if (m) refresh.push(Number(m[1]));
    }
    if (refresh.length >= 3) break;
    await new Promise((r) => setTimeout(r, 5000));
  }
  console.log(`  scheduled status refresh (ms): ${refresh.join(", ")}`);

  fs.writeFileSync(outPath, JSON.stringify({ label, rows, scheduleId, movieId, refreshMs: refresh, endpoints: results }, null, 2));
  console.log("saved", outPath);
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
