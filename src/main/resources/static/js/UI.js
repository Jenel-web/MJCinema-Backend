export class UI {
  showPopup(message, color) {
    const popup = document.getElementById("status-popup");

    popup.classList.remove("popup-green", "popup-red");

    popup.innerText = message;

    // We use the "green" label to decide which CSS class to add
    if (color === "green") {
      popup.classList.add("popup-green");
    } else if (color === "red") {
      popup.classList.add("popup-red");
    }
    popup.classList.add("show");

    // 4. Auto-hide after 3 seconds
    setTimeout(() => {
      popup.classList.remove("show");
    }, 3000);
  }

  showError(msg) {
    const feedback = document.getElementById("auth-feedback");
    const container = document.querySelector(".login-container");

    feedback.innerText = msg;
    feedback.className = "feedback-visible error-vibe"; // Swaps to our floating box

    // Add the shake to the container for impact
    container.classList.add("shake");
    setTimeout(() => container.classList.remove("shake"), 400);

    // AUTO-HIDE after 3 seconds so they can type again
    setTimeout(() => {
      feedback.className = "feedback-hidden";
    }, 3000);
  }

  hideFeedback() {
    const feedback = document.getElementById("auth-feedback");
    if (feedback.classList.contains("feedback-visible")) {
      feedback.className = "feedback-hidden";
    }
  }
}
