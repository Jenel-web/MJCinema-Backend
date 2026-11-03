// Prints a before / after table from two benchmark.js result files.
//   node measure/compare.js before.json after.json
const fs = require("node:fs");
const [beforeFile, afterFile] = process.argv.slice(2);
const before = JSON.parse(fs.readFileSync(beforeFile, "utf8"));
const after = JSON.parse(fs.readFileSync(afterFile, "utf8"));

const fmt = (ms) => (ms >= 1000 ? `${(ms / 1000).toFixed(2)} s` : `${ms} ms`);
const change = (b, a) => {
  const pct = ((a - b) / b) * 100;
  if (Math.abs(pct) < 10) return "same";
  return pct < 0 ? `${(b / a).toFixed(1)}x faster` : `${(a / b).toFixed(1)}x slower`;
};

console.log("| Endpoint | Before (median) | After (median) | Change | SQL statements |");
console.log("|---|---|---|---|---|");
for (const [name, b] of Object.entries(before.endpoints)) {
  const a = after.endpoints[name];
  if (!a) continue;
  const q = b.queries === a.queries ? `${b.queries}` : `${b.queries} -> ${a.queries}`;
  console.log(`| ${name} | ${fmt(b.medianMs)} | ${fmt(a.medianMs)} | ${change(b.medianMs, a.medianMs)} | ${q} |`);
}
for (const [name, a] of Object.entries(after.endpoints)) {
  if (!before.endpoints[name]) console.log(`| ${name} | (not measured) | ${fmt(a.medianMs)} | new | ${a.queries} |`);
}
const avg = (a) => (a.length ? Math.round(a.reduce((x, y) => x + y, 0) / a.length) : null);
console.log(`\nScheduled status refresh: before ${before.refreshMs.join(", ")} ms (avg ${avg(before.refreshMs)}), after ${after.refreshMs.join(", ")} ms (avg ${avg(after.refreshMs)})`);
