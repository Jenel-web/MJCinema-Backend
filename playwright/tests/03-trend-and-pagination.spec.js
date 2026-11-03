const { test, expect, request } = require("@playwright/test");
const { dayFromNow, createHelpers } = require("./helpers");

let api;
let h;
let admin;

// the Monday of the week a yyyy-mm-dd date falls in (the trend is keyed by that Monday)
function mondayOf(dateText) {
  const [y, m, d] = dateText.split("-").map(Number);
  const date = new Date(y, m - 1, d);
  date.setDate(date.getDate() - ((date.getDay() + 6) % 7)); // getDay(): 0 = Sunday ... 6 = Saturday
  const pad = (n) => String(n).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

const trend = async () => (await (await api.get("/ticket/getSalesTrend", h.auth(admin))).json());
const page = (query) => api.get(`/ticket/showBookings${query}`, h.auth(admin));

test.beforeAll(async () => {
  api = await request.newContext({ baseURL: "http://localhost:8080" });
  h = createHelpers(api);
  admin = await h.login("admin1");
});

test.describe("weekly sales trend (grouped by the database)", () => {
  test("counts bookings per week, keyed by Monday, and leaves cancelled tickets out", async () => {
    const dayA = dayFromNow(10);
    const dayB = dayFromNow(24); // 14 days later, so always another week
    expect(mondayOf(dayA)).not.toBe(mondayOf(dayB));
    const scheduleA = await h.addSchedule(admin, 1, "MORNING", dayA);
    const scheduleB = await h.addSchedule(admin, 1, "MORNING", dayB);
    const before = await trend();

    const user = await h.newUser();
    const codes = {};
    for (const seat of ["A1", "A2", "A3"]) codes[seat] = (await (await h.book(user, scheduleA, [seat])).json()).ticketCode;
    for (const seat of ["B1", "B2"]) expect((await h.book(user, scheduleB, [seat])).ok()).toBeTruthy();
    expect((await h.cancelTicket(user, codes.A3)).ok()).toBeTruthy(); // 3 booked in week A, 1 cancelled

    const after = await trend();
    const gained = (day) => (after[mondayOf(day)] || 0) - (before[mondayOf(day)] || 0);
    // the old code compared a String with an enum, so a cancelled ticket was still counted (it would say 3)
    expect(gained(dayA)).toBe(2);
    expect(gained(dayB)).toBe(2);
  });

  test("every key is a Monday, the weeks are in order, and the values are numbers", async () => {
    const data = await trend();
    const keys = Object.keys(data);
    expect(keys.length).toBeGreaterThan(0);
    for (const key of keys) {
      expect(key).toMatch(/^\d{4}-\d{2}-\d{2}$/);
      expect(mondayOf(key)).toBe(key); // a Monday maps to itself
      expect(Number.isInteger(data[key])).toBe(true);
    }
    expect(keys).toEqual([...keys].sort());
  });
});

test.describe("bookings list (paginated)", () => {
  test("returns one page, newest first, with the total in the X-Total-Count header", async () => {
    const scheduleId = await h.addSchedule(admin, 2, "NIGHT", dayFromNow(11));
    const user = await h.newUser();
    const ticket = await (await h.book(user, scheduleId, ["C1"])).json();

    const res = await page("?page=0&size=1");
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body).toHaveLength(1);
    expect(body[0].ticketCode).toBe(ticket.ticketCode); // the booking made a moment ago is the newest
    expect(Number(res.headers()["x-total-count"])).toBeGreaterThanOrEqual(1);
    expect(Object.keys(body[0]).sort()).toEqual(
      ["cinemaName", "seatNumber", "showDate", "startTime", "ticketCode", "ticketStatus", "username"].sort(),
    ); // the shape of one booking did not change
  });

  test("walking every page returns each booking exactly once", async () => {
    // make sure there are several pages, whatever ran before: ten more tickets in one booking
    const scheduleId = await h.addSchedule(admin, 3, "EVENING", dayFromNow(12));
    const user = await h.newUser();
    const seats = ["C1", "C2", "C3", "C4", "D1", "D2", "D3", "D4", "A1", "A2"]; // the seed cinema has 4 seats per row
    expect((await h.book(user, scheduleId, seats)).ok()).toBeTruthy();

    const first = await page("?page=0&size=7");
    const total = Number(first.headers()["x-total-count"]);
    expect(total).toBeGreaterThan(7);

    const codes = [];
    for (let p = 0; ; p++) {
      const body = await (await page(`?page=${p}&size=7`)).json();
      if (body.length === 0) break;
      expect(body.length).toBeLessThanOrEqual(7);
      codes.push(...body.map((b) => b.ticketCode));
      expect(p).toBeLessThan(1000); // safety net against an endless loop
    }
    expect(codes).toHaveLength(total);
    expect(new Set(codes).size).toBe(total); // no booking twice, none missing
  });

  test("a page past the end is empty but still reports the total", async () => {
    const first = await page("?page=0&size=5");
    const total = first.headers()["x-total-count"];
    const res = await page("?page=100000&size=5");
    expect(res.status()).toBe(200);
    expect(await res.json()).toEqual([]);
    expect(res.headers()["x-total-count"]).toBe(total);
  });

  test("the page size has a default of 20 and a cap of 100, and bad numbers are tidied up", async () => {
    const total = Number((await page("?page=0&size=1")).headers()["x-total-count"]);

    expect(await (await page("")).json()).toHaveLength(Math.min(total, 20)); // no parameters: 20
    expect(await (await page("?size=100000")).json()).toHaveLength(Math.min(total, 100)); // capped at 100
    expect(await (await page("?size=0")).json()).toHaveLength(1); // below 1 becomes 1
    const negative = await (await page("?page=-3&size=3")).json();
    expect(negative).toEqual(await (await page("?page=0&size=3")).json()); // a negative page is page 0
  });

  test("only admins can read it", async () => {
    const noToken = await api.get("/ticket/showBookings", { maxRedirects: 0 });
    expect(noToken.status()).toBe(401);
    const user = await h.newUser();
    const normalUser = await api.get("/ticket/showBookings", { ...h.auth(user), maxRedirects: 0 });
    // a logged-in user who is not an admin is forbidden (403), and gets no data
    expect(normalUser.status()).toBe(403);
    expect(Array.isArray(await normalUser.json())).toBe(false);
    expect(normalUser.headers()["x-total-count"]).toBeUndefined();
  });

  test("a browser is allowed to read X-Total-Count (CORS exposes it)", async () => {
    const res = await api.get("/ticket/showBookings?size=1", {
      headers: { ...h.auth(admin).headers, Origin: "http://127.0.0.1:5500" },
    });
    expect(res.status()).toBe(200);
    expect(res.headers()["access-control-expose-headers"]).toContain("X-Total-Count");
  });
});
