class CinemaApp {
  constructor() {
    this.auth = new Auth();
    this.ui = new UI();
    this.movies = new Movies();
    this.init();
  }

  init() {
    this.setupAuthListeners();
    this.loadMoviesIfNeeded();
  }

  setupAuthListeners() {
    const registerBtn = document.querySelector("#register-button");
    if (registerBtn) {
      registerBtn.addEventListener("click", async (e) => {
        e.preventDefault();

        this.ui.hideFeedback();
        // 3. GET the data
        const user = document.querySelector("#reg-username").value;
        const pass = document.querySelector("#reg-password").value;
        const con = document.querySelector("#confirm-password").value;

        if (pass !== con) {
          this.ui.showError("Passwords do not match.");
          return;
        }
        const count = pass.length;
        if (count < 8) {
          this.ui.showError(
            "Password length should be more than 8 characters."
          );
          return;
        }
        const result = await this.auth.register(user, pass, con);

        if (result.success) {
          this.ui.showPopup("Account Created. Redirecting...", "green");
          setTimeout(() => {
            window.location.href = "index.html";
          }, 2000);
        } else {
          this.ui.showPopup("Server is offline. Try again later.", "red");
        }
      });
    }

    const loginBtn = document.querySelector("#login-button");
    if (loginBtn) {
      loginBtn.addEventListener("click", async (e) => {
        e.preventDefault();

        this.ui.hideFeedback();

        const user = document.querySelector("#username").value;
        const pass = document.querySelector("#password").value;

        const result = await this.auth.login(user, pass);

        if (result.success) {
          this.ui.showPopup(result.message, "green");
          window.location.href = "dashboard.html";
        } else if (result.message === "Server is offline. Try again later.") {
          this.ui.showPopup("Server is offline. Try again later.", "red");
        } else {
          this.ui.showError("Invalid Username or Password.");
        }
      });
    }
  }

  loadMoviesIfNeeded() {
    if (document.getElementById("movie-grid")) {
      this.movies.loadMovies(`${this.movies.baseUrl}/schedule/now-showing`);
    }
  }
}

// Initialize the app when DOM is ready
document.addEventListener("DOMContentLoaded", () => {
  new CinemaApp();
});
