// Fills the throwaway test database with a small, believable cinema so the README screenshots show real data.
// Everything except cinemas, seats and movies goes through the real API, so it exercises the real booking code.
// The movies, cinemas and users are made up.
const { execFileSync } = require("node:child_process");

const API = process.env.API_URL || "http://localhost:8080";
const MYSQL = process.env.MYSQL_BIN || "C:/mysql-client/bin/mysql.exe";
const DB_PORT = process.env.TEST_DB_PORT || "3307";
const DB_NAME = process.env.TEST_DB_NAME || "mjcinema_test";

const sql = (statement) =>
  execFileSync(MYSQL, ["--host=127.0.0.1", `--port=${DB_PORT}`, "--user=root", DB_NAME, "-e", statement], { stdio: "pipe" });

const pad = (n) => String(n).padStart(2, "0");
const dayFromNow = (n) => {
  const d = new Date();
  d.setDate(d.getDate() + n);
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
};

async function call(method, path, body, token) {
  const res = await fetch(API + path, {
    method,
    headers: { "Content-Type": "application/json", ...(token ? { Authorization: `Bearer ${token}` } : {}) },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  const text = await res.text();
  if (!res.ok) throw new Error(`${method} ${path} -> ${res.status} ${text}`);
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

const MOVIES = [
  ["Night Harbor", 8.4], ["Deep Quiet", 8.2], ["Ember Road", 8.0], ["The Last Signal", 7.8],
  ["Kite Season", 7.6], ["Hollow Crown", 7.4], ["Low Tide", 7.2], ["Copper Sky", 7.0],
  ["Paper Lanterns", 7.4], ["Salt Line", 7.2], ["Afterglow", 7.0],
];
const OVERVIEW =
  "A harbor pilot takes one last job and learns the cargo is the crew. With the port closing at dawn, every hour on the water costs her something she cannot get back.";

// the frontend prices seats by row: A-B VIP, G-H Balcony, everything else Regular
const categoryOf = (row) => (row === "A" || row === "B" ? "VIP" : row === "G" || row === "H" ? "BALCONY" : "REGULAR");

function cinemaSql(id, location, name, rows, layout) {
  const perRow = [...layout].filter((c) => c === "S").length;
  const seats = [];
  for (let r = 0; r < rows; r++) {
    const row = String.fromCharCode(65 + r);
    for (let n = 1; n <= perRow; n++) seats.push(`(${id}, '${categoryOf(row)}', '${row}${n}')`);
  }
  sql(
    `INSERT INTO cinema (location, cinema_name, total_rows, total_columns, total_seats, seat_layout) VALUES ('${location}', '${name}', ${rows}, ${layout.length}, ${rows * perRow}, '${layout}');` +
      `INSERT INTO seat (cinema_id, seat_category, seat_number) VALUES ${seats.join(",")};`,
  );
}

async function seedDemo() {
  sql(
    "SET FOREIGN_KEY_CHECKS=0; TRUNCATE tickets; TRUNCATE seat_price; TRUNCATE schedule; TRUNCATE seat; TRUNCATE movie; TRUNCATE cinema; TRUNCATE users; SET FOREIGN_KEY_CHECKS=1;",
  );
  cinemaSql(1, "SM City North", "Cinema 1", 10, "SSSS_SSSSSS_SSSS");
  cinemaSql(2, "Ayala Harbor Point", "Cinema 2", 8, "SSS_SSSS_SSS");
  const values = MOVIES.map(
    ([title, rating], i) => `(${i + 1}, '/p${i + 1}.jpg', '${title}', '2 hrs', ${rating}, '${OVERVIEW}', 'INACTIVE', '2026-0${(i % 9) + 1}-10')`,
  );
  sql(`INSERT INTO movie (tmdb_id, poster, title, duration, rating, overview, status, release_date) VALUES ${values.join(",")};`);

  // accounts: one admin, one "main" demo user and a few more so the booked seats come from different people
  const accounts = ["admin1", "demo.user", "alex.reyes", "bea.santos", "carlo.lim", "dina.cruz", "eli.tan"];
  for (const username of accounts) {
    await call("POST", "/user/register", { username, password: "pass1234", confirm: "pass1234" });
  }
  sql("UPDATE users SET role='ADMIN' WHERE username='admin1'; UPDATE users SET balance=5000 WHERE username<>'admin1';");
  const sessions = {};
  for (const username of accounts) sessions[username] = await call("POST", "/user/login", { username, password: "pass1234" });
  const admin = sessions.admin1;

  // showtimes (tomorrow and the next few days), then three movies that are only "coming soon"
  const plan = [
    [1, 1, 1, "MORNING"], [1, 1, 1, "MIDDAY"], [1, 1, 1, "EVENING"], [1, 1, 1, "NIGHT"], [1, 2, 1, "AFTERNOON"], [1, 2, 1, "EVENING"],
    [2, 2, 1, "NIGHT"], [2, 2, 2, "MORNING"], [3, 1, 1, "AFTERNOON"], [3, 2, 1, "MIDDAY"],
    [4, 1, 2, "MORNING"], [4, 1, 2, "EVENING"], [5, 2, 2, "AFTERNOON"], [5, 2, 2, "NIGHT"],
    [6, 1, 3, "NIGHT"], [7, 2, 3, "EVENING"], [8, 1, 4, "AFTERNOON"],
    [9, 1, 9, "NIGHT"], [10, 2, 12, "EVENING"], [11, 1, 15, "EVENING"],
  ];
  for (const [movieId, cinemaId, day, slot] of plan) {
    await call("POST", "/schedule/add", { movieId, cinemaId, showDate: dayFromNow(day), slot, vipPrice: 450, regPrice: 320, balPrice: 260 }, admin.jwtToken);
  }

  const find = async (movieId, cinemaLocation, day, slot) => {
    const list = await call("GET", `/schedule/movieSchedules/${movieId}`);
    const s = list.find((x) => x.showDate === dayFromNow(day) && x.slot === slot && x.cinema.location === cinemaLocation);
    if (!s) throw new Error(`schedule not found: movie ${movieId} ${cinemaLocation} ${slot}`);
    return s.scheduleId;
  };
  const book = (who, scheduleId, seats) =>
    call("POST", "/ticket/book", { userId: sessions[who].userId, scheduleId, selectedSeat: seats }, sessions[who].jwtToken);

  // the headline showtime: Night Harbor, Cinema 1, tomorrow evening, with a believable crowd
  const headline = await find(1, "SM City North", 1, "EVENING");
  await book("alex.reyes", headline, ["D5", "D6", "D7", "D8"]);
  await book("bea.santos", headline, ["E3", "E4", "F9", "F10"]);
  await book("carlo.lim", headline, ["A5", "A6", "B8", "B9"]);
  await book("dina.cruz", headline, ["C2", "C3", "G11", "G12"]);
  await book("eli.tan", headline, ["E9", "E10", "E11", "H2"]);

  // the main demo user's bookings: three seats together, one more movie, and one cancelled ticket
  const night = await find(1, "SM City North", 1, "NIGHT");
  const ember = await find(3, "Ayala Harbor Point", 1, "MIDDAY");
  const kite = await find(5, "Ayala Harbor Point", 2, "AFTERNOON");
  await book("demo.user", night, ["D6", "D7"]);
  await book("demo.user", night, ["A3"]);
  await book("demo.user", ember, ["H5"]);
  const cancelled = await book("demo.user", kite, ["C4"]);
  await call("PATCH", "/ticket/cancel", { ticketCode: cancelled.ticketCode }, sessions["demo.user"].jwtToken);

  // a few more sales on other showtimes so the admin charts and seat stats have something to show
  const deep = await find(2, "Ayala Harbor Point", 1, "NIGHT");
  const last = await find(4, "SM City North", 2, "EVENING");
  await book("alex.reyes", deep, ["B2", "B3", "D4"]);
  await book("bea.santos", last, ["C6", "C7", "C8"]);
  await book("carlo.lim", last, ["F4", "F5"]);

  return { sessions, headline, night };
}

module.exports = { seedDemo, call, API };
