# 🎬 Movie Ticket Booking Application

**Author:** Mark Jenel Cortas

**Level:** 2nd Year IT Student - Personal Project

**Tech Stack:** Java, Spring Boot, Spring Data JPA, MySQL, Spring Security

## 🚀 Project Overview

This is a robust backend REST API for a Movie Theater management system. It allows users to browse movies and book tickets (VIP or Regular), while providing an administrative layer for managing movie listings and viewing booking statistics. This project was developed as a challenge to go beyond my university curriculum and master industry-standard backend architecture.

## ✨ Key Features

* **User Management:** Secure registration and authentication using **Spring Security** and **BCrypt** password hashing.
* **Role-Based Access Control (RBAC):** * `USER`: Can register, book tickets, and view/cancel their own bookings.
* `ADMIN`: Can add/remove movies and view all tickets sold for specific films.


* **Seat Management:** Automated tracking of VIP and Regular seat availability with real-time seat-limit validation.
* **Global Error Handling:** Implemented `@ControllerAdvice` to provide clean, consistent JSON error responses (avoiding messy stack traces for the end-user).
* **Data Integrity:** Utilized `@Transactional` to ensure that ticket creation and seat count updates happen as a single atomic operation.

## 🛠 Technical Highlights

* **DTO Pattern:** Used Data Transfer Objects to decouple the database entities from the API layer, improving security and performance.
* **Custom JPQL Queries:** Optimized database interactions using constructor expressions in `JpaRepository` to fetch only the data needed for the UI.
* **N-Tier Architecture:** Organized the codebase into Controller, Service, Repository, and Entity layers for maximum maintainability and clear separation of concerns.

## 🧠 Learning Outcomes & Personal Growth

This project represents a significant milestone in my journey as a self-taught developer. Beyond my formal 2nd-year IT curriculum, I took the initiative to study and implement several advanced concepts independently:

* **Self-Taught OOP Principles:** While I learned the basics in school, I independently mastered the application of **Inheritance, Encapsulation, and Polymorphism** within a production-ready framework.
* **Deep Dive into Spring Security:** I self-studied the internal workings of `SecurityFilterChain` and `UserDetailsService` to implement a custom authentication flow.
* **The DTO Pattern:** I researched and implemented DTOs to solve the problem of exposing sensitive database structures to the API layer—a concept typically not taught until senior-level courses.
* **Problem Solving & Debugging:** Encountered and resolved real-world database constraints, such as **MySQL Safe Update Mode**, by researching documentation and implementing session-based SQL toggles.
* **Autonomous Learning:** Almost 90% of the architecture and logic in this project was self-studied through technical documentation and trial-and-error, proving my ability to adapt to new technologies rapidly.

## 🚦 API Endpoints (Samples)

### User & Booking Endpoints

| Method | Endpoint | Description | Access |
| --- | --- | --- | --- |
| `POST` | `/user/register` | Register a new account | Public |
| `POST` | `/ticket/book` | Book a movie ticket | USER |
| `GET` | `/ticket/booked` | View current user's tickets | USER |
| `DELETE` | `/ticket/cancel/{code}` | Cancel a specific ticket | USER |

### Admin Endpoints

| Method | Endpoint | Description | Access |
| --- | --- | --- | --- |
| `POST` | `/admin/addMovie` | Add a new movie listing | ADMIN |
| `GET` | `/admin/listMovie` | View all movies in database | ADMIN |
| `DELETE` | `/admin/removeMovie/{id}` | Remove a movie and its tickets | ADMIN |

## 🧪 How to Run

1. **Clone the repository.**
2. **Configure MySQL:** Update `src/main/resources/application.properties` with your DB username and password.
3. **Run the App:** Execute `./mvnw spring-boot:run` in your terminal.
4. **Test:** Use Postman or cURL to interact with the endpoints at `http://localhost:8080`.

