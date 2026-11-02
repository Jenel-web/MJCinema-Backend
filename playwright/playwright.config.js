const { defineConfig } = require("@playwright/test");

// the app is started by hand on localhost:8080 against the throwaway test database (see seed.sh).
// The spec files share that database and run in file-name order, hence the 01-, 02- prefixes.
module.exports = defineConfig({
  testDir: "./tests",
  workers: 1,
  timeout: 60000,
  reporter: [["list"]],
  use: {
    baseURL: "http://localhost:8080",
    headless: true,
    screenshot: "only-on-failure",
  },
});
