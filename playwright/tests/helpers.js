const { expect } = require("@playwright/test");

const PRICES = { vipPrice: 100, regPrice: 50, balPrice: 20 };

// local date as yyyy-mm-dd (toISOString would use UTC and can be a day off)
function dayFromNow(days) {
  const d = new Date();
  d.setDate(d.getDate() + days);
  const pad = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

// small helpers around the api, shared by the spec files
function createHelpers(api) {
  const stamp = Date.now();
  let counter = 0;

  const auth = (u) => ({ headers: { Authorization: `Bearer ${u.token}` } });

  async function login(username, password = "pass1234") {
    const res = await api.post("/user/login", { data: { username, password } });
    expect(res.ok()).toBeTruthy();
    const body = await res.json();
    return { token: body.jwtToken, userId: body.userId, username };
  }

  async function newUser(prefix = "user") {
    const name = `${prefix}${stamp}_${counter++}`;
    const res = await api.post("/user/register", {
      data: { username: name, password: "pass1234", confirm: "pass1234" },
    });
    expect(res.ok()).toBeTruthy();
    return login(name);
  }

  async function balance(u) {
    const res = await api.get(`/user/getUser?userId=${u.userId}`, auth(u));
    return (await res.json()).balance;
  }

  async function addSchedule(admin, movieId, slot, day, prices = PRICES) {
    const res = await api.post("/schedule/add", {
      ...auth(admin),
      data: { movieId, cinemaId: 1, showDate: day, slot, ...prices },
    });
    expect(res.ok(), await res.text()).toBeTruthy();
    // movieSchedules lists the active schedules of the movie, so find the new one by date and slot
    const list = await (await api.get(`/schedule/movieSchedules/${movieId}`)).json();
    const found = list.find((s) => s.showDate === day && s.slot === slot);
    expect(found, "new schedule should be listed").toBeTruthy();
    return found.scheduleId;
  }

  const book = (u, scheduleId, seats) =>
    api.post("/ticket/book", {
      ...auth(u),
      data: { userId: u.userId, scheduleId, selectedSeat: seats },
    });

  const cancelTicket = (u, ticketCode) =>
    api.patch("/ticket/cancel", { ...auth(u), data: { ticketCode } });

  const cancelSchedule = (admin, scheduleId) =>
    api.patch(`/schedule/cancel/${scheduleId}`, auth(admin));

  const occupied = async (scheduleId) =>
    (await (await api.get(`/seat/occupied/${scheduleId}`)).json()).sort();

  return { auth, login, newUser, balance, addSchedule, book, cancelTicket, cancelSchedule, occupied };
}

module.exports = { PRICES, dayFromNow, createHelpers };
