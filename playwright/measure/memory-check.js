// Calls the two heavy admin endpoints once and reports status, time and size. Run it against an app started
// with the same memory limit as production (Dockerfile: -Xmx300m):
//   java -Xmx300m -jar app.jar     then     node measure/memory-check.js
const API = process.env.API_URL || "http://localhost:8080";

(async () => {
  const login = await fetch(`${API}/user/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username: "admin1", password: "pass1234" }),
  });
  const token = (await login.json()).jwtToken;

  for (const path of ["/ticket/getSalesTrend", "/ticket/showBookings", "/ticket/showBookings?page=0&size=20"]) {
    const t0 = performance.now();
    try {
      const res = await fetch(API + path, { headers: { Authorization: `Bearer ${token}` } });
      const body = await res.text();
      const ms = Math.round(performance.now() - t0);
      const msg = res.ok ? "" : "  " + body.slice(0, 140).replace(/\s+/g, " ");
      console.log(`${path.padEnd(40)} HTTP ${res.status}  ${String(ms).padStart(6)} ms  ${(body.length / 1024).toFixed(1).padStart(10)} KB${msg}`);
    } catch (e) {
      console.log(`${path.padEnd(40)} FAILED: ${e.cause?.code || e.message}`);
    }
  }
})();
