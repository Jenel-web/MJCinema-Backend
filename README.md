🎬 MJ Cinema | Full-Stack Boutique Booking System
A premium, end-to-end movie ticketing application built with Java (Spring Boot) and Vanilla JavaScript. This project focuses on a "boutique" user experience, featuring real-time seat selection, transactional integrity, and custom error handling.

✨ Key Features
- Dynamic Seat Selection: Interactive UI for selecting specific seats with real-time category-based pricing.

- Full-Stack Integration: Seamless communication between a RESTful Spring Boot API and a responsive frontend.

- Transactional Booking: Ensures data integrity—seats are only reserved if the user's balance deduction is successful.

- Global Exception Handling: A robust backend "safety net" that provides meaningful feedback to the user (e.g., "Insufficient Balance" or "Seat Taken").

- Boutique Aesthetic: A custom-designed dark/gold theme tailored for a high-end cinema experience.

🛠️ Tech Stack
Backend: Java 25, Spring Boot 3.5.9, Spring Security, Spring Data JPA.

Frontend: HTML5, CSS3 (Custom Boutique Theme), Vanilla JavaScript (ES6+).

Database: MySQL / Hibernate ORM.

Security: CORS configuration and session-based logic.

🚀 Technical Highlights (The "Hard Parts")
- DTO Pattern: Implemented Data Transfer Objects to decouple the database schema from the API response, enhancing security and performance.

- Custom Global Exception Handler: Built a @RestControllerAdvice to intercept RuntimeExceptions and transform them into structured JSON for the frontend.

- Template Literals & Async/Await: Leveraged modern JavaScript for clean, readable asynchronous API calls.

📸 Preview

<img width="1345" height="564" alt="image" src="https://github.com/user-attachments/assets/376bf458-f2b8-4710-905c-3b48a0e8547c" />
<img width="1363" height="561" alt="image" src="https://github.com/user-attachments/assets/9961874b-8e81-4e2c-aaff-31647bb710a5" />


⚙️ Setup & Installation
Backend:

Navigate to /MJCinema.

Configure your application.properties with your MySQL credentials.

Run ./mvnw spring-boot:run.

Frontend:

Open index.html via Live Server.

Ensure the baseUrl in mjcinema.js matches your backend port (default: 8080).
