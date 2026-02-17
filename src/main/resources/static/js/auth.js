import { UI } from "./ui.js";
const ui = new UI(); //imports UI and instantiates

export class Auth {
  constructor() {
    this.baseUrl = "http://localhost:8080";
    // Initialize listeners when the class is instantiated
    this.initListeners();
  }

  // Inside a class, you don't use the 'function' keyword
  async authData(url, data) {
    try {
      const response = await fetch(url, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
      });

      const result = await response.json();
      if (response.ok) {
        return { success: true, data: result };
      } else {
        return {
          success: false,
          message: result.message || "An error occurred",
        };
      }
    } catch (error) {
      return { success: false, message: "Server is offline. Try again later." };
    }
  }

  initListeners() {
    const registerBtn = document.querySelector("#register-button");
    const loginBtn = document.querySelector("#login-button");

    if (registerBtn) {
      registerBtn.addEventListener("click", (e) => this.handleRegister(e));
    }

    if (loginBtn) {
      loginBtn.addEventListener("click", (e) => this.handleLogin(e));
    }
  }

  async handleRegister(e) {
    e.preventDefault();
    this.clearFeedback();

    const user = document.querySelector("#reg-username").value;
    const pass = document.querySelector("#reg-password").value;
    const con = document.querySelector("#confirm-password").value;

    if (pass !== con) {
      ui.showError("Passwords do not match.");
      return;
    }

    if (pass.length < 8) {
      ui.showError("Password length should be more than 8 characters.");
      return;
    }

    const result = await this.authData(`${this.baseUrl}/user/register`, {
      username: user,
      password: pass,
      confirm: con,
    });

    if (result.success) {
      ui.showPopup("Account Created. Redirecting...", "green");
      setTimeout(() => {
        window.location.href = "index.html";
      }, 2000);
    } else {
      ui.showPopup(result.message, "red");
    }
  }

  async handleLogin(e) {
    e.preventDefault();
    this.clearFeedback();

    const user = document.querySelector("#username").value;
    const pass = document.querySelector("#password").value;

    const result = await this.authData(`${this.baseUrl}/user/login`, {
      username: user,
      password: pass,
    });

    if (result.success) {
      localStorage.setItem("userId", result.data.userId);
      localStorage.setItem("username", result.data.username);
      ui.showPopup("Login successful!", "green");
      window.location.href = "dashboard.html";
    } else {
      const msg =
        result.message === "Server is offline. Try again later."
          ? result.message
          : "Invalid Username or Password.";
      ui.showError(msg);
    }
  }

  clearFeedback() {
    const feedback = document.getElementById("auth-feedback");
    if (feedback && feedback.classList.contains("feedback-visible")) {
      feedback.className = "feedback-hidden";
    }
  }
}
