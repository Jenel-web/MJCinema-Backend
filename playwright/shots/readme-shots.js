// Generates the screenshots used in the root README.md into ../docs/screenshots.
//
// It drives the real React app (MJCinema-frontend, sibling repo) against the real backend, with demo data.
// Before running:
//   1. a throwaway MySQL on port 3307 with an empty `mjcinema_test` database
//   2. the backend running on http://localhost:8080 against that database
//   3. the frontend dev server:  cd "../MJCinema - Frontend/web" && VITE_API_URL=http://localhost:8080 npm run dev -- --host 127.0.0.1 --port 5500 --strictPort
// Then:  npm run shots        (this wipes the throwaway database and fills it with demo data)
const path = require("node:path");
const fs = require("node:fs");
const { chromium } = require("@playwright/test");
const { seedDemo } = require("./demo-data");
const { posterPng } = require("./png");

const APP = process.env.APP_URL || "http://127.0.0.1:5500";
const OUT = path.resolve(__dirname, "../../docs/screenshots");
const HUES = [2, 212, 275, 160, 32, 340, 190, 48, 300, 120, 20];

const DESKTOP = { viewport: { width: 1440, height: 900 } };
const MOBILE = { viewport: { width: 390, height: 844 }, hasTouch: true, deviceScaleFactor: 2 };

async function open(browser, device, session, extraStorage = {}) {
  const ctx = await browser.newContext(device);
  // TMDB posters are replaced by generated gradient art (never ship real poster images)
  await ctx.route("**/image.tmdb.org/**", (route) => {
    const n = Number(/p(\d+)\.jpg/.exec(route.request().url())?.[1] || 1);
    return route.fulfill({ contentType: "image/png", body: posterPng(HUES[(n - 1) % HUES.length]) });
  });
  if (session) {
    await ctx.addInitScript((data) => {
      for (const [k, v] of Object.entries(data)) localStorage.setItem(k, v);
    }, { token: session.jwtToken, userId: String(session.userId), username: session.username, ...extraStorage });
  }
  return { ctx, page: await ctx.newPage() };
}

async function settle(page, ms = 1800) {
  await page.waitForLoadState("networkidle").catch(() => {});
  await page.evaluate(() => document.fonts.ready);
  await page.waitForTimeout(ms);
}

const shot = async (page, name) => {
  await page.screenshot({ path: path.join(OUT, `${name}.png`) });
  console.log("saved", name);
};

(async () => {
  fs.mkdirSync(OUT, { recursive: true });
  const { sessions, headline } = await seedDemo();
  console.log("demo data ready");
  const browser = await chromium.launch();

  // sign in (the real form, so the screenshot shows the real screen)
  {
    const { ctx, page } = await open(browser, DESKTOP, null);
    await page.goto(APP + "/");
    await settle(page, 600);
    await shot(page, "login");
    await ctx.close();
  }

  // dashboard: featured movie + poster grid
  {
    const { ctx, page } = await open(browser, DESKTOP, sessions["demo.user"]);
    await page.goto(APP + "/dashboard");
    await page.getByRole("button", { name: "Book seats" }).first().waitFor();
    await settle(page);
    await shot(page, "dashboard");

    // movie details with the showtime picker
    await page.getByRole("button", { name: "Book seats" }).first().click();
    const group = page.getByRole("group", { name: "SM City North" });
    await group.waitFor();
    await group.getByRole("button").nth(2).click();
    await settle(page, 900);
    await shot(page, "movie-showtimes");
    await ctx.close();
  }

  // seat booking: a busy showtime, three seats picked
  {
    const { ctx, page } = await open(browser, DESKTOP, sessions["demo.user"], { selectedScheduleId: String(headline) });
    await page.goto(APP + "/bookseat");
    await page.getByRole("button", { name: /^Seat F6,/ }).waitFor();
    for (const seat of ["F6", "F7", "F8"]) await page.getByRole("button", { name: new RegExp(`^Seat ${seat},`) }).click();
    await settle(page, 600);
    await shot(page, "seat-booking");
    await ctx.close();
  }

  // my bookings
  {
    const { ctx, page } = await open(browser, DESKTOP, sessions["demo.user"]);
    await page.goto(APP + "/tickets");
    await page.getByText(/Night Harbor/i).first().waitFor();
    await settle(page);
    await shot(page, "my-bookings");
    await ctx.close();
  }

  // admin console
  {
    const { ctx, page } = await open(browser, DESKTOP, sessions.admin1);
    for (const [route, name] of [["/admin", "admin-home"], ["/admin/movies", "admin-movies"], ["/admin/schedules", "admin-schedules"]]) {
      await page.goto(APP + route);
      await settle(page, 2500);
      await shot(page, name);
    }
    await ctx.close();
  }

  // phone layouts
  {
    const { ctx, page } = await open(browser, MOBILE, sessions["demo.user"]);
    await page.goto(APP + "/dashboard");
    await page.getByRole("button", { name: "Book seats" }).first().waitFor();
    await settle(page);
    await shot(page, "mobile-dashboard");
    await ctx.close();
  }
  {
    const { ctx, page } = await open(browser, MOBILE, sessions["demo.user"], { selectedScheduleId: String(headline) });
    await page.goto(APP + "/bookseat");
    await page.getByRole("button", { name: /^Seat F6,/ }).waitFor();
    for (const seat of ["F6", "F7"]) await page.getByRole("button", { name: new RegExp(`^Seat ${seat},`) }).click();
    await settle(page, 600);
    await shot(page, "mobile-seat-booking");
    await ctx.close();
  }

  // brand card: palette and type from the frontend's design tokens (web/src/index.css, design/system/tokens.md)
  {
    const { ctx, page } = await open(browser, { viewport: { width: 1200, height: 450 }, deviceScaleFactor: 2 }, null);
    await page.goto(APP + "/"); // loads the same Google fonts the app uses
    await settle(page, 400);
    await page.evaluate(() => {
      const swatches = [
        ["#08080A", "Page", "bg"], ["#121216", "Cards", "s1"], ["#D91F27", "Projector red", "rd"],
        ["#E6B25D", "Gold, VIP", "gd"], ["#6EA8FE", "Balcony", "bl"], ["#3ECF8E", "Success", "ok"],
        ["#F5A524", "Coming soon", "wn"], ["#F5F5F6", "Text", "tx"],
      ];
      const chips = swatches.map(([hex, name, token]) =>
        `<div style="width:130px"><div style="height:84px;border-radius:14px;background:${hex};border:1px solid rgba(255,255,255,.16)"></div>` +
        `<div style="margin-top:10px;font:600 14px 'DM Sans'">${name}</div>` +
        `<div style="margin-top:2px;font:400 13px 'DM Sans';color:#A6A6B0">${hex} · ${token}</div></div>`).join("");
      document.body.innerHTML =
        `<div style="box-sizing:border-box;width:1200px;height:450px;padding:44px 52px;background:radial-gradient(60% 300px at 50% 0%, rgba(217,31,39,.28), transparent 70%), #08080A;color:#F5F5F6">` +
        `<div style="display:flex;justify-content:space-between;align-items:flex-end">` +
        `<div style="font:800 64px/0.95 'Barlow Condensed';text-transform:uppercase;letter-spacing:.015em">Dark canvas.<br><span style="color:#D91F27">One red.</span> A little gold.</div>` +
        `<div style="text-align:right;color:#A6A6B0;font:400 15px/1.5 'DM Sans'"><b style="color:#F5F5F6;font-weight:600">Barlow Condensed</b> 800 for headings<br><b style="color:#F5F5F6;font-weight:600">DM Sans</b> 400–700 for everything else</div></div>` +
        `<div style="display:flex;gap:18px;margin-top:44px">${chips}</div>` +
        `<div style="margin-top:34px;color:#A6A6B0;font:400 14px/1.5 'DM Sans'">Red is only for the primary action, the active state and the logo. Gold marks anything premium.</div></div>`;
      document.body.style.margin = "0";
    });
    await page.evaluate(() => document.fonts.ready);
    await page.waitForTimeout(500);
    await shot(page, "brand");
    await ctx.close();
  }

  await browser.close();
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
