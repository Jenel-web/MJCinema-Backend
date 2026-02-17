let priceList = {}; //pricelist where the data we fetched will go
let selectedSeats = []; //initialized the selected seats which is empty at first

async function initBooking() {
  const scheduleId = localStorage.getItem("selectedScheduleId");

  if (!scheduleId) {
    console.error("No Schedule ID found! Redirecting to home...");
    window.location.href = "index.html";
    return;
  }
  // 1. Fetch Schedule (including Cinema layout)
  const response = await fetch(`http://localhost:8080/schedule/${scheduleId}`);
  const schedule = await response.json();

  // 2. Setup the Grid based on Cinema dimensions
  const cinema = schedule.cinema;
  if (schedule && schedule.movie) {
    document.getElementById("movie-title").innerText = schedule.movie.title;
  } else {
    console.error("Movie data is missing in the schedule response!", schedule);
    document.getElementById("movie-title").innerText = "Title Unavailable";
  }
  //get the prices
  const result = await fetch(
    `http://localhost:8080/schedule/schedPrice/${scheduleId}`
  );
  const prices = await result.json();
  const selected = await getOccupiedSeats(scheduleId); //makes the array of strings of selected seats

  prices.forEach((item) => {
    priceList[item.seatCategory] = item.price;
  }); //initializes the prices of the tickets

  renderBlueprint(
    cinema.totalRows,
    cinema.totalColumns,
    cinema.seatLayout,
    selected
  ); //takes the arguments
}

async function getOccupiedSeats(scheduleId) {
  const response = await fetch(
    `http://localhost:8080/seat/occupied/${scheduleId}`
  );

  const occupiedSeats = await response.json();

  return occupiedSeats;
}
function renderBlueprint(rows, cols, layoutString, occupiedSeats = []) {
  const grid = document.getElementById("seat-grid");

  // Set columns based on the length of the string (including underscores)
  grid.style.setProperty("--grid-cols", layoutString.length);
  grid.innerHTML = "";

  for (let r = 0; r < rows; r++) {
    const rowLetter = String.fromCharCode(65 + r); //loops from A to the number of rows
    let seatNumberInRow = 1; // Reset for every new row (A, B, C...)

    for (let i = 0; i < layoutString.length; i++) {
      //this takes the length of the layout String which counts the S and the Blank
      const char = layoutString[i]; //takes the character in the layoutString to verify

      if (char === "_") {
        // If underscore, create an invisible spacer
        const spacer = document.createElement("div"); //makes a div for the empty space
        spacer.className = "aisle-spacer";
        grid.appendChild(spacer);
      } else {
        // If 'S', create a seat and give it the next number
        const seatId = `${rowLetter}${seatNumberInRow}`; //assigns the seat Id
        const seat = document.createElement("div"); //creates an element div
        seat.className = "seat"; //configured and styled in css based on className

        if (occupiedSeats.includes(seatId)) {
          // checks from the data fetched if its already occupied
          seat.classList.add("occupied"); //adds the classList name
        } else {
          seat.onclick = () => handleSeatClick(seat, seatId);
        }

        grid.appendChild(seat); //gr
        seatNumberInRow++; // Increment only for actual seats
      }
    }
  }
}

function handleSeatClick(seatElement, seatId) {
  if (seatElement.classList.contains("selected")) {
    seatElement.classList.remove("selected"); //if already selected, it will be unselected
    selectedSeats = selectedSeats.filter((id) => id !== seatId); //filters the selected seats
    //makes it a new one where the seat id of unselected will not be there.
  } else {
    seatElement.classList.add("selected");
    selectedSeats.push(seatId);
  }
  updateUI();
}

function updateUI() {
  let total = 0;

  // Calculate total based on row category
  selectedSeats.forEach((id) => {
    const row = id.charAt(0); //the row will be the char O which is (A, B, C..)
    let category = "REGULAR"; //makes it regular for default
    if (row === "A" || row === "B") category = "VIP";
    if (row === "G" || row === "H") category = "BALCONY";

    total += priceList[category] || 0; //adds to the total
  });

  document.getElementById("display-seats").innerText =
    selectedSeats.join(", ") || "None";
  document.getElementById("display-total").innerText = total.toLocaleString();
}

document.addEventListener("DOMContentLoaded", () => {
  initBooking();
});

// ticket booking
async function bookTicket(selectedSeats = []) {
  const id = localStorage.getItem("selectedScheduleId");
  const user = localStorage.getItem("userId");
  const data = {
    userId: Number(user),
    scheduleId: Number(id), //its a string when received in the localStorage
    selectedSeat: selectedSeats,
  }; //maps the data to the java post method

  console.log("Sending Data:", JSON.stringify(data));
  try {
    const response = await fetch("http://localhost:8080/ticket/book", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
      credentials: "include",
    });

    if (response.ok) {
      const result = await response.json();
      showStatus(
        true,
        "Booking Confirmed!",
        "Your seats are secured. Enjoy the show!"
      ); //says the message
    } else {
      const result = await response.json(); //jsonify the response
      // 'result.message' comes from the GlobalExceptionHandler
      showStatus(
        false,
        "Booking Failed",
        result.message || "An unexpected error occurred."
      );
    }
  } catch (error) {
    console.error("Error : " + error);
  }
}
function showStatus(isSuccess, title, message) {
  const modal = document.getElementById("status-modal");
  const card = modal.querySelector(".status-card");
  const titleEl = document.getElementById("status-title");
  const msgEl = document.getElementById("status-message");
  const iconContainer = document.getElementById("status-icon-container");
  const okBtn = document.getElementById("status-ok-btn");
  //make the html and get the elements then make the pop Ups
  // Set Content
  titleEl.innerText = title;
  msgEl.innerText = message;

  // Remove old states and apply new one
  card.classList.remove("success", "error");

  if (isSuccess) {
    card.classList.add("success");
    iconContainer.innerHTML = "🎟️";
  } else {
    card.classList.add("error");
    iconContainer.innerHTML = "❌";
  }

  modal.style.display = "flex";

  okBtn.onclick = () => { //does this when the button is clicked
    modal.style.display = "none";
    // Redirect logic remains
    if (isSuccess) window.location.href = "dashboard.html";
  };
}
const bookBtn = document.querySelector(".btn-confirm");

bookBtn.addEventListener("click", (e) => {
  e.preventDefault();

  if (selectedSeats.length === 0) {
    alert("Please select atleast one seat.");
    return;
  }

  const modal = document.getElementById("confirm-modal");
  modal.style.display = "flex"; // Show the modal

  document.getElementById("cancel-btn").onclick = () => {
    modal.style.display = "none";
  };

  // Handle "Yes, Book Now"
  document.getElementById("confirm-btn").onclick = async () => {
    modal.style.display = "none"; // Hide it
    await bookTicket(selectedSeats); // Trigger your Java API
  };
});
