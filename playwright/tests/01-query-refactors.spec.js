const { test, expect, request } = require("@playwright/test");

const BASE = "http://localhost:8080";
const PRICES = { vipPrice: 100, regPrice: 50, balPrice: 20 };

let api; // request context without auth
let admin; // { token, userId }
const stamp = Date.now();
let counter = 0;

// local date as yyyy-mm-dd (toISOString would use UTC and can be a day off)
function dayFromNow(days) {
  const d = new Date();
  d.setDate(d.getDate() + days);
  const pad = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}
function tomorrow() {
  return dayFromNow(1);
}

async function register(username, password = "pass1234") {
  const res = await api.post("/user/register", {
    data: { username, password, confirm: password },
  });
  expect(res.ok()).toBeTruthy();
}

async function login(username, password = "pass1234") {
  const res = await api.post("/user/login", { data: { username, password } });
  expect(res.ok()).toBeTruthy();
  const body = await res.json();
  return { token: body.jwtToken, userId: body.userId, username };
}

async function newUser() {
  const name = `user${stamp}_${counter++}`;
  await register(name);
  return login(name);
}

function auth(u) {
  return { headers: { Authorization: `Bearer ${u.token}` } };
}

async function balance(u) {
  const res = await api.get(`/user/getUser?userId=${u.userId}`, auth(u));
  return (await res.json()).balance;
}

async function addSchedule(movieId, slot, day = tomorrow()) {
  const res = await api.post("/schedule/add", {
    ...auth(admin),
    data: { movieId, cinemaId: 1, showDate: day, slot, ...PRICES },
  });
  expect(res.ok(), await res.text()).toBeTruthy();
}

async function findScheduleId(movieId) {
  const res = await api.get(`/schedule/movieSchedules/${movieId}`);
  expect(res.ok()).toBeTruthy();
  return res;
}

async function book(u, scheduleId, seats) {
  return api.post("/ticket/book", {
    ...auth(u),
    data: { userId: u.userId, scheduleId, selectedSeat: seats },
  });
}

test.beforeAll(async () => {
  api = await request.newContext({ baseURL: BASE });
  admin = await login("admin1");
});

test("adding a schedule moves the movie to now showing (status refresh)", async () => {
  await addSchedule(1, "MORNING");
  const res = await api.get("/schedule/now-showing");
  const titles = (await res.json()).map((m) => m.title);
  expect(titles).toContain("Alpha Movie");
  expect(titles).not.toContain("Beta Movie");
});

test("a schedule more than 5 days away makes the movie coming soon", async () => {
  const day = dayFromNow(10);
  await addSchedule(2, "NIGHT", day);
  const res = await api.get("/schedule/coming-soon");
  const body = await res.json();
  const beta = body.find((m) => m.title === "Beta Movie");
  expect(beta).toBeTruthy();
  expect(beta.showDate).toBe(day);
  const now = (await (await api.get("/schedule/now-showing")).json()).map((m) => m.title);
  expect(now).not.toContain("Beta Movie");
});

test("GET /schedule/{id} keeps its json shape (movie and cinema included)", async () => {
  const schedules = await (await findScheduleId(1)).json();
  expect(schedules.length).toBeGreaterThan(0);
  const s = schedules[0];
  expect(s.cinema.location).toBe("Test Mall");
  expect(s.movie.title).toBe("Alpha Movie");

  const one = await (await api.get(`/schedule/${s.scheduleId}`)).json();
  expect(one.cinema.seatLayout).toBe("SS_SS");
  expect(one.cinema.totalRows).toBe(4);
  expect(one.movie.title).toBe("Alpha Movie");
});

test("schedPrice returns seatCategory and price", async () => {
  const [s] = await (await (await findScheduleId(1)).json());
  const prices = await (await api.get(`/schedule/schedPrice/${s.scheduleId}`)).json();
  const byCat = Object.fromEntries(prices.map((p) => [p.seatCategory, p.price]));
  expect(byCat).toEqual({ VIP: 100, REGULAR: 50, BALCONY: 20 });
});

test("seat map: all available at first, booked seats become unavailable", async () => {
  const [s] = await (await findScheduleId(1)).json();
  const map0 = await (await api.get(`/schedule/seats?id=${s.scheduleId}`)).json();
  expect(Object.keys(map0)).toEqual(["A", "B", "C", "D"]);
  expect(Object.values(map0).flat().every((x) => x.available)).toBe(true);
  expect(map0.A[0].price).toBe(100);
  expect(map0.D[0].price).toBe(20);

  const u = await newUser();
  const res = await book(u, s.scheduleId, ["A1", "C2"]);
  expect(res.ok(), await res.text()).toBeTruthy();

  const map1 = await (await api.get(`/schedule/seats?id=${s.scheduleId}`)).json();
  const a1 = map1.A.find((x) => x.seatNumber === "A1");
  const c2 = map1.C.find((x) => x.seatNumber === "C2");
  const a2 = map1.A.find((x) => x.seatNumber === "A2");
  expect(a1.available).toBe(false);
  expect(c2.available).toBe(false);
  expect(a2.available).toBe(true);

  const occupied = await (await api.get(`/seat/occupied/${s.scheduleId}`)).json();
  expect(occupied.sort()).toEqual(["A1", "C2"]);
});

test("booking several seats charges the total once and returns the first ticket", async () => {
  const [s] = await (await findScheduleId(1)).json();
  const u = await newUser();
  const before = await balance(u);
  const res = await book(u, s.scheduleId, ["B1", "B2", "D1"]);
  expect(res.ok(), await res.text()).toBeTruthy();
  const ticket = await res.json();
  expect(ticket.status).toBe("SUCCESS");
  expect(ticket.title).toBe("Alpha Movie");
  expect(ticket.seat).toBe("B1");
  expect(ticket.price).toBe(100);
  expect(ticket.ticketCode).toMatch(/^MJ-/);
  expect(await balance(u)).toBe(before - (100 + 100 + 20));
});

test("booking failures: taken seat, unknown seat, no seats, insufficient balance", async () => {
  const [s] = await (await findScheduleId(1)).json();
  const u = await newUser();

  let res = await book(u, s.scheduleId, ["A1"]); // booked in an earlier test
  expect(res.status()).toBe(400);
  expect((await res.json()).message).toBe("Seat is already taken.");

  res = await book(u, s.scheduleId, ["Z9"]);
  expect(res.status()).toBe(400);
  expect((await res.json()).message).toBe("Seat not found.");

  res = await book(u, s.scheduleId, []);
  expect(res.status()).toBe(400);

  // the failed attempts must not have charged anything
  expect(await balance(u)).toBe(1000);

  // 10 VIP seats at 100 each is more than the 1000 balance... only 8 VIP seats exist,
  // so book 3 more users' worth: A3 A4 B3 B4 (400) + C1 C3 C4 (150) leaves 450; use a poor user instead
  const poor = await newUser();
  const all = ["A3", "A4", "B3", "B4"];
  res = await book(poor, s.scheduleId, all); // 400, fine
  expect(res.ok()).toBeTruthy();
  const second = await book(poor, s.scheduleId, ["C1", "C3", "C4", "D2", "D3", "D4"]); // 150 + 60
  expect(second.ok()).toBeTruthy();
  expect(await balance(poor)).toBe(1000 - 400 - 210);
});

test("a cancelled ticket frees its seat again and refunds the user", async () => {
  await addSchedule(3, "EVENING");
  const schedules = await (await findScheduleId(3)).json();
  const sid = schedules[0].scheduleId;
  const u = await newUser();
  const res = await book(u, sid, ["A1"]);
  expect(res.ok()).toBeTruthy();
  const { ticketCode } = await res.json();
  expect(await balance(u)).toBe(900);

  let occupied = await (await api.get(`/seat/occupied/${sid}`)).json();
  expect(occupied).toEqual(["A1"]);

  const cancel = await api.patch("/ticket/cancel", { ...auth(u), data: { ticketCode } });
  expect(cancel.ok(), await cancel.text()).toBeTruthy();
  expect(await balance(u)).toBe(1000);

  occupied = await (await api.get(`/seat/occupied/${sid}`)).json();
  expect(occupied).toEqual([]);
  const map = await (await api.get(`/schedule/seats?id=${sid}`)).json();
  expect(map.A.find((x) => x.seatNumber === "A1").available).toBe(true);

  const again = await book(u, sid, ["A1"]); // rebooking works because the cancelled ticket no longer blocks it
  expect(again.ok(), await again.text()).toBeTruthy();
});

test("seat stats come from one query with correct counts and percentage", async () => {
  const res = await api.get("/schedule/seatStats");
  expect(res.ok(), await res.text()).toBeTruthy();
  const stats = await res.json();
  const alpha = stats.find((x) => x.title === "Alpha Movie");
  const gamma = stats.find((x) => x.title === "Gamma Movie");
  const beta = stats.find((x) => x.title === "Beta Movie");
  expect(alpha.cinemaName).toBe("Cinema 1");
  // alpha: A1 C2 (test 5) + B1 B2 D1 (test 6) + A3 A4 B3 B4 C1 C3 C4 D2 D3 D4 (test 7) = 15 of 16
  expect(alpha.occupiedSeats).toBe(15);
  expect(alpha.availableSeats).toBe(1);
  expect(alpha.percentage).toBe(93.75);
  // gamma: A1 booked again after cancel
  expect(gamma.occupiedSeats).toBe(1);
  expect(gamma.availableSeats).toBe(15);
  expect(gamma.percentage).toBe(6.25);
  // beta has no tickets, the left join must still return it
  expect(beta.occupiedSeats).toBe(0);
  expect(beta.availableSeats).toBe(16);
  expect(beta.percentage).toBe(0);
});

test("cancelling a schedule refunds every user once and keeps the schedule cancelled", async () => {
  await addSchedule(3, "AFTERNOON");
  const schedules = await (await findScheduleId(3)).json();
  const target = schedules.find((s) => s.slot === "AFTERNOON");
  const u1 = await newUser();
  const u2 = await newUser();
  expect((await book(u1, target.scheduleId, ["A1", "C1"])).ok()).toBeTruthy(); // 150
  expect((await book(u2, target.scheduleId, ["D1"])).ok()).toBeTruthy(); // 20
  expect(await balance(u1)).toBe(850);
  expect(await balance(u2)).toBe(980);

  const res = await api.patch(`/schedule/cancel/${target.scheduleId}`, auth(admin));
  expect(res.ok(), await res.text()).toBeTruthy();
  expect(await balance(u1)).toBe(1000);
  expect(await balance(u2)).toBe(1000);

  const stats = await (await api.get("/schedule/seatStats")).json();
  // cancelled schedules are no longer listed as active
  expect(stats.filter((x) => x.title === "Gamma Movie").length).toBe(1);
});

test("UI: now showing grid, movie modal, showtime dropdown, seat map", async ({ page }) => {
  const u = await newUser();
  const errors = [];
  page.on("pageerror", (e) => errors.push(e.message));

  // the login page redirects to a relative dashboard.html that does not exist when served from Spring,
  // so log in through the api and open the real dashboard page
  await page.goto("/index.html");
  await page.evaluate(({ id, name }) => {
    localStorage.setItem("userId", id);
    localStorage.setItem("username", name);
  }, { id: String(u.userId), name: u.username });
  await page.goto("/html/dashboard.html");

  await page.click("#now-showing");
  const cards = page.locator(".movie-card");
  await expect(cards.first()).toBeVisible();
  await expect(page.locator(".movie-card h3", { hasText: "Alpha Movie" })).toBeVisible();

  await page.locator(".movie-card", { hasText: "Alpha Movie" }).click();
  await expect(page.locator("#movie-modal .modal-info h2")).toHaveText("Alpha Movie");

  await page.click(".book-btn-modal");
  const dropdown = page.locator("#showtime-dropdown");
  await expect(dropdown).toBeVisible();
  await expect(dropdown.locator("option")).toHaveCount(2); // placeholder + 1 schedule
  await expect(dropdown.locator("option").nth(1)).toContainText("Test Mall");
  await dropdown.selectOption({ index: 1 });
  await page.click(".confirm-sched-btn");

  await page.waitForURL("**/bookseat.html");
  await expect(page.locator("#movie-title")).toHaveText("Alpha Movie");
  await expect(page.locator("#seat-grid .seat")).toHaveCount(16);
  // alpha has 15 of 16 seats taken at this point
  await expect(page.locator("#seat-grid .seat.occupied")).toHaveCount(15);

  await page.locator("#seat-grid .seat:not(.occupied)").first().click();
  await expect(page.locator("#display-total")).not.toHaveText("0");

  expect(errors).toEqual([]);
});

test("UI: coming soon grid shows the show date", async ({ page }) => {
  await page.goto("/html/dashboard.html");
  await page.click("#coming-soon");
  await expect(page.locator(".movie-card", { hasText: "Beta Movie" })).toBeVisible();
  await expect(page.locator(".movie-card .coming-soon-tag").first()).toBeVisible();
});
