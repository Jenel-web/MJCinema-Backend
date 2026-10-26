<div align="center">

<img src="src/main/resources/static/images/MJCinema%20logo.png" alt="MJ Cinema logo" width="140" />

# 🎬 MJ Cinema

**A full-stack boutique movie-booking system — Spring Boot API, JWT security, and a hand-built dark/gold frontend.**

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-6DB33F?logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-6DB33F?logo=springsecurity&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED?logo=docker&logoColor=white)
![JavaScript](https://img.shields.io/badge/Vanilla%20JS-ES6%2B-F7DF1E?logo=javascript&logoColor=black)

</div>

---

## 📸 Preview

<img width="1345" height="564" alt="MJ Cinema home" src="https://github.com/user-attachments/assets/376bf458-f2b8-4710-905c-3b48a0e8547c" />
<img width="1363" height="561" alt="MJ Cinema booking" src="https://github.com/user-attachments/assets/9961874b-8e81-4e2c-aaff-31647bb710a5" />

---

## ✨ What it does

MJ Cinema covers the whole ticketing lifecycle: browsing what's showing, picking exact seats, paying from an in-app wallet, cancelling for a refund — and, for admins, running the cinema from a live analytics dashboard.

### For moviegoers
- **Now Showing & Coming Soon** — browse current and upcoming screenings across multiple cinemas.
- **Interactive seat map** — pick specific seats; taken seats are marked from live data.
- **Tiered pricing** — `VIP`, `REGULAR` and `BALCONY` seats, each priced per schedule.
- **Wallet-based payment** — the ticket price is deducted from the user's balance at booking.
- **My Tickets & cancellation** — view bookings, cancel, and get the money back automatically.
- **Account tools** — register, log in, change password.

### For admins
- **Movie management** — add movies manually or import metadata from **TMDB**.
- **Cinema & schedule management** — add cinemas, create showtimes, cancel schedules.
- **Seat pricing** — set the price of each seat category per schedule.
- **Analytics dashboard** — total sales, revenue today, sales trend, total bookings, user count, active movies, average revenue per schedule, seat-occupancy stats, a movie **leaderboard**, and a **best showtime** report.

---

## 🧠 Engineering highlights

| Area | What's under the hood |
|---|---|
| **Transactional integrity** | Booking and cancellation run in a single `@Transactional` unit: seat check → balance check → balance deduction → ticket creation. If any step fails, everything rolls back — no half-paid, half-booked states. Cancellation refunds the wallet in the same transaction. |
| **Double-booking protection** | Seat availability is verified server-side before a ticket is issued, returning a clear *"Seat is already taken"* error instead of relying on the UI. |
| **Schedule conflict detection** | A JPQL `existsOverlappingSchedule` query rejects any showtime that collides with another in the same cinema, date and time window. |
| **Stateless JWT auth** | `JwtFilter` + `JWTService` (jjwt 0.12) authenticate each request; sessions are `STATELESS`; passwords are hashed with **BCrypt**. |
| **Role-based access** | `@EnableMethodSecurity` with `@PreAuthorize("hasRole('ADMIN')")` guards every management and analytics endpoint. Public browsing, authenticated booking. |
| **Global exception handling** | A `@RestControllerAdvice` turns exceptions into structured JSON so the frontend can show meaningful messages (*Insufficient balance*, *Schedule overlapping*, …). |
| **DTO pattern** | ~40 purpose-built DTOs keep entities off the wire; most reports are built with constructor-expression JPQL (`SELECT new …DTO(…)`) straight from the database. |
| **Third-party integration** | `TmdbService` calls the TMDB API to search movies, fetch details and list popular titles. |
| **Deployment-ready** | Multi-stage `Dockerfile` (Maven build → slim JRE) with JVM flags tuned for a 512 MB host such as Render; port is read from `$PORT`. |

---

## 🏗️ Architecture

```
┌────────────────────┐   fetch + JWT   ┌──────────────────────────────────────┐
│  Vanilla JS client │ ──────────────▶ │  Spring Boot REST API                │
│  (static/ html·js) │ ◀────────────── │                                      │
└────────────────────┘      JSON       │  JwtFilter → Controllers → Services  │
                                       │        │                    │        │
                                       │   GlobalExceptionHandler   TMDB API  │
                                       │                    ▼                 │
                                       │        Spring Data JPA Repositories  │
                                       └──────────────────┬───────────────────┘
                                                          ▼
                                                       MySQL
```

**Domain model:** `Users` · `Cinema` · `Movie` · `Schedule` · `Seat` · `SeatPrice` · `Tickets`
with status enums `MovieStatus`, `ScheduleStatus`, `TicketStatus` (`ACTIVE`, `CANCELLED`, `REFUNDED`, `COMPLETED`, `EXPIRED`) and `SeatCategory`.

### Project layout

```
src/main/java/com/MovieBookingApplication/MJCinema/
├── Config/        Security, CORS, project beans
├── Controllers/   Cinema · Movie · Schedule · Seat · Tickets · Tmdb · User
├── DTO/           Request / response models
├── Entity/        JPA entities and enums
├── Filters/       JwtFilter
├── Repository/    Spring Data JPA + custom JPQL
└── Services/      Business logic, JWT, TMDB client, exception handler

src/main/resources/static/
├── index.html · register.html     Landing & sign-up
├── html/                          bookseat.html, dashboard.html (admin)
├── js/                            auth · UI · bookseat · main · mjcinema
└── css/style.css                  Dark / gold boutique theme
```

---

## 🔌 API overview

| Base path | Access | Highlights |
|---|---|---|
| `POST /user/register`, `/user/login` | Public | Create an account, receive a JWT |
| `/user/**` | Authenticated | Profile, tickets, `PATCH /user/change/password` |
| `/schedule/now-showing`, `/coming-soon`, `/seats`, `/{id}` | Public | Browse screenings |
| `/schedule/add`, `PATCH /schedule/cancel/{id}` | **Admin** | Manage showtimes |
| `/schedule/seatStats`, `/bestShowtime`, `/showAverageRevenue`, … | Public | Scheduling analytics |
| `/movie/show`, `/movie/leaderboard`, `/movie/tmdb/{search,details,popular}` | Public | Catalogue and TMDB lookups |
| `POST /movie/add` | **Admin** | Add a movie |
| `/cinema/all`, `/cinema/movies/{id}` | Public | Cinemas and their movies |
| `/seat/occupied/{id}` | Public | Taken seats for a schedule |
| `POST /ticket/book`, `PATCH /ticket/cancel`, `GET /ticket/mytickets` | Authenticated | Booking flow |
| `/ticket/totalSales`, `/showBookings`, `/showRevenueToday`, `/getSalesTrend` | **Admin** | Sales reporting |

---

## 🚀 Getting started

### Prerequisites
- JDK 17+
- A MySQL 8 database
- A [TMDB API key](https://www.themoviedb.org/settings/api) (for movie import)

### 1. Configure
Set your own values in `src/main/resources/application.properties` (or override with environment variables):

```properties
spring.datasource.url=jdbc:mysql://<host>:<port>/mjcinema
spring.datasource.username=<user>
spring.datasource.password=<password>

tmdb.api.key=<your-tmdb-key>
tmdb.base.url=https://api.themoviedb.org/3
server.port=${PORT:8080}
```

Hibernate runs with `ddl-auto=update`, so tables are created on first start.

### 2. Run

```bash
./mvnw spring-boot:run
```

The API and the static frontend are both served from `http://localhost:8080` — open `/index.html`.

### 3. Or run with Docker

```bash
docker build -t mjcinema .
docker run -p 8080:8080 mjcinema
```

---

## 🗺️ Roadmap ideas
- Payment gateway instead of wallet balance
- Email / QR ticket confirmation
- Seat-hold timer while checking out
- Integration tests for the booking transaction

---

<div align="center">

Built by **Mark Jenel Cortas** · Made with ☕ and a love of cinema.

</div>
