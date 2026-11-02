const { test, expect, request } = require("@playwright/test");
const { PRICES, dayFromNow, createHelpers } = require("./helpers");

let api;
let h;
let admin;
const day2 = dayFromNow(2);
const day3 = dayFromNow(3);

test.beforeAll(async () => {
  api = await request.newContext({ baseURL: "http://localhost:8080" });
  h = createHelpers(api);
  admin = await h.login("admin1");
});

test("cancelling a schedule also cancels its tickets and frees the seats", async () => {
  const sid = await h.addSchedule(admin, 1, "AFTERNOON", day2);
  const u = await h.newUser();
  expect((await h.book(u, sid, ["A1", "C1"])).ok()).toBeTruthy();
  expect(await h.balance(u)).toBe(850);

  expect((await h.cancelSchedule(admin, sid)).ok()).toBeTruthy();
  expect(await h.balance(u)).toBe(1000);

  const mine = await (await api.get(`/ticket/mytickets?userId=${u.userId}`, h.auth(u))).json();
  expect(mine.map((t) => t.ticketStatus)).toEqual(["CANCELLED", "CANCELLED"]);
  expect(await h.occupied(sid)).toEqual([]);

  // a cancelled schedule is no longer offered for booking
  const list = await (await api.get("/schedule/movieSchedules/1")).json();
  expect(list.map((s) => s.scheduleId)).not.toContain(sid);
  const late = await h.book(u, sid, ["B1"]);
  expect(late.status()).toBe(400);
  expect((await late.json()).message).toBe("Schedule is not available for booking.");
});

test("cancelling the same schedule twice does not refund twice", async () => {
  const sid = await h.addSchedule(admin, 1, "MIDDAY", day2);
  const u = await h.newUser();
  expect((await h.book(u, sid, ["A1"])).ok()).toBeTruthy();
  expect(await h.balance(u)).toBe(900);

  expect((await h.cancelSchedule(admin, sid)).ok()).toBeTruthy();
  expect(await h.balance(u)).toBe(1000);

  const again = await h.cancelSchedule(admin, sid);
  expect(again.status()).toBe(400);
  expect((await again.json()).message).toBe("Schedule cannot be cancelled.");
  expect(await h.balance(u)).toBe(1000);
});

test("cancelling the same ticket twice does not refund twice", async () => {
  const sid = await h.addSchedule(admin, 1, "EVENING", day2);
  const u = await h.newUser();
  const t1 = (await (await h.book(u, sid, ["A1"])).json()).ticketCode;
  const t2 = (await (await h.book(u, sid, ["A2"])).json()).ticketCode;
  expect(await h.balance(u)).toBe(800);

  // one after the other
  expect((await h.cancelTicket(u, t1)).ok()).toBeTruthy();
  const second = await h.cancelTicket(u, t1);
  expect(second.status()).toBe(400);
  expect((await second.json()).message).toBe("Ticket cannot be cancelled.");
  expect(await h.balance(u)).toBe(900);

  // both at the same moment: exactly one of them is refunded
  const results = await Promise.all([h.cancelTicket(u, t2), h.cancelTicket(u, t2)]);
  expect(results.map((r) => r.status()).sort()).toEqual([200, 400]);
  expect(await h.balance(u)).toBe(1000);
});

test("two users booking the same seat at the same moment: exactly one wins", async () => {
  const sid = await h.addSchedule(admin, 2, "MORNING", day2);
  const users = [];
  for (let i = 0; i < 6; i++) users.push(await h.newUser());

  // all six users go for A1 at once
  const results = await Promise.all(users.map((u) => h.book(u, sid, ["A1"])));
  const statuses = results.map((r) => r.status());
  expect(statuses.filter((s) => s === 200)).toHaveLength(1);
  expect(statuses.filter((s) => s === 400)).toHaveLength(5);
  expect(await h.occupied(sid)).toEqual(["A1"]);

  // only the winner paid
  const balances = await Promise.all(users.map((u) => h.balance(u)));
  expect(balances.filter((b) => b === 900)).toHaveLength(1);
  expect(balances.filter((b) => b === 1000)).toHaveLength(5);
});

test("several seats, several users, all at once: no seat is sold twice", async () => {
  const sid = await h.addSchedule(admin, 2, "MIDDAY", day2);
  const users = [];
  for (let i = 0; i < 4; i++) users.push(await h.newUser());
  const seats = ["B1", "B2", "B3"];

  // every user tries all three seats in separate requests
  const attempts = [];
  for (const u of users) for (const s of seats) attempts.push(h.book(u, sid, [s]));
  const results = await Promise.all(attempts);
  expect(results.filter((r) => r.ok())).toHaveLength(3);
  expect(await h.occupied(sid)).toEqual(seats);
});

test("one user cannot overspend with two simultaneous bookings", async () => {
  const sid = await h.addSchedule(admin, 2, "AFTERNOON", day2, { vipPrice: 400, regPrice: 50, balPrice: 20 });
  const u = await h.newUser();

  // each booking costs 800 and the user has 1000, so only one can go through
  const results = await Promise.all([h.book(u, sid, ["A1", "A2"]), h.book(u, sid, ["B1", "B2"])]);
  expect(results.map((r) => r.status()).sort()).toEqual([200, 400]);
  const failed = results.find((r) => r.status() === 400);
  expect((await failed.json()).message).toBe("Insufficient balance.");
  expect(await h.balance(u)).toBe(200);
});

test("a garbage token is treated as logged out: 401 json, not a 500 and not a redirect", async () => {
  const res = await api.get("/ticket/mytickets?userId=1", {
    headers: { Authorization: "Bearer not.a.token" },
    maxRedirects: 0,
  });
  expect(res.status()).toBe(401);
  expect((await res.json()).message).toBe("Please log in to continue.");
});

test("no token, or the 'Bearer null' the new front end sends when logged out, also gets a 401", async () => {
  for (const headers of [{}, { Authorization: "Bearer null" }]) {
    const res = await api.post("/ticket/book", {
      headers,
      data: { userId: 1, scheduleId: 1, selectedSeat: ["A1"] },
      maxRedirects: 0,
    });
    expect(res.status()).toBe(401);
    expect(res.headers()["content-type"]).toContain("application/json");
  }
});

test("the 401 carries the CORS headers, otherwise a browser would hide it as a network error", async () => {
  const origin = "http://127.0.0.1:5500";
  const res = await api.get("/ticket/mytickets?userId=1", {
    headers: { Origin: origin, Authorization: "Bearer null" },
    maxRedirects: 0,
  });
  expect(res.status()).toBe(401);
  expect(res.headers()["access-control-allow-origin"]).toBe(origin);
});

test("a valid token still works after the change, and public endpoints need no token", async () => {
  const u = await h.newUser();
  const mine = await api.get(`/ticket/mytickets?userId=${u.userId}`, h.auth(u));
  expect(mine.status()).toBe(200);
  expect((await api.get("/schedule/now-showing", { maxRedirects: 0 })).status()).toBe(200);
  expect((await api.get("/movie/show", { maxRedirects: 0 })).status()).toBe(200);
});

test("dashboard.html and bookseat.html are served from the site root", async () => {
  const dash = await api.get("/dashboard.html");
  expect(dash.status()).toBe(200);
  expect(await dash.text()).toContain("Now Showing");
  const seats = await api.get("/bookseat.html");
  expect(seats.status()).toBe(200);
  expect(await seats.text()).toContain("seat-grid");
});

test("UI: real login lands on the dashboard and keeps the token", async ({ page }) => {
  const u = await h.newUser();
  await page.goto("/index.html");
  await page.fill("#username", u.username);
  await page.fill("#password", "pass1234");
  await page.click("#login-button");
  await page.waitForURL("**/dashboard.html");
  const stored = await page.evaluate(() => ({
    token: localStorage.getItem("jwtToken"),
    userId: localStorage.getItem("userId"),
  }));
  expect(stored.token).toBeTruthy();
  expect(stored.userId).toBe(String(u.userId));
});

test("UI: log in, pick a showtime, book a seat end to end", async ({ page }) => {
  const sid = await h.addSchedule(admin, 3, "NIGHT", day3);
  const u = await h.newUser();
  const errors = [];
  page.on("pageerror", (e) => errors.push(e.message));

  await page.goto("/index.html");
  await page.fill("#username", u.username);
  await page.fill("#password", "pass1234");
  await page.click("#login-button");
  await page.waitForURL("**/dashboard.html");

  await page.click("#now-showing");
  await page.locator(".movie-card", { hasText: "Gamma Movie" }).click();
  await page.click(".book-btn-modal");
  const dropdown = page.locator("#showtime-dropdown");
  await expect(dropdown).toBeVisible();
  // the schedule cancelled earlier in this file/phase 3 must not be offered
  const offered = await dropdown.locator("option").evaluateAll((o) => o.map((x) => x.value));
  expect(offered).toContain(String(sid));
  await dropdown.selectOption(String(sid));
  await page.click(".confirm-sched-btn");

  await page.waitForURL("**/bookseat.html");
  await expect(page.locator("#movie-title")).toHaveText("Gamma Movie");
  await expect(page.locator("#seat-grid .seat")).toHaveCount(16);
  await expect(page.locator("#seat-grid .seat.occupied")).toHaveCount(0);

  await page.locator("#seat-grid .seat").first().click(); // A1 is a VIP seat: 100
  await expect(page.locator("#display-seats")).toHaveText("A1");
  await expect(page.locator("#display-total")).toHaveText("100");

  await page.click("#confirm-booking-btn");
  await page.click("#confirm-btn");
  await expect(page.locator("#status-title")).toHaveText("Booking Confirmed!");
  await page.click("#status-ok-btn");
  await page.waitForURL("**/dashboard.html");

  expect(await h.balance(u)).toBe(900);
  expect(await h.occupied(sid)).toEqual(["A1"]);
  expect(errors).toEqual([]);
});

test("UI: a missing token shows 'Session Expired' and sends the user to login", async ({ page }) => {
  const sid = await h.addSchedule(admin, 3, "MORNING", day3);
  const u = await h.newUser();

  await page.goto("/index.html");
  await page.evaluate(({ id, schedule }) => {
    localStorage.clear(); // logged in before but the token is gone
    localStorage.setItem("userId", id);
    localStorage.setItem("selectedScheduleId", schedule);
  }, { id: String(u.userId), schedule: String(sid) });
  await page.goto("/bookseat.html");

  await expect(page.locator("#seat-grid .seat")).toHaveCount(16);
  await page.locator("#seat-grid .seat").first().click();
  await page.click("#confirm-booking-btn");
  await page.click("#confirm-btn");
  await expect(page.locator("#status-title")).toHaveText("Session Expired");
  await page.click("#status-ok-btn");
  await page.waitForURL("**/index.html");

  expect(await h.balance(u)).toBe(1000); // nothing was booked or charged
  expect(await h.occupied(sid)).toEqual([]);
});
