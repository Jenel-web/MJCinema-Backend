# Performance measurements: Phase 4, steps 0 and 1

What was measured, how, and what it says. Step 0 is the measuring tools. Step 1 is the five indexes.

## How it was measured

| | |
|---|---|
| **Data** | A generated, repeatable data set (`playwright/measure/seed-big.sql`): 40 cinemas, 300 movies, 14,000 showtimes (a third in the future), 42,000 seat prices, **280,000 tickets**, 3,000 users. |
| **Setup** | Throwaway MySQL 8 on the same machine as the app, so there is **no network delay** in these numbers. One request at a time. |
| **Tools** | `TimingFilter` logs the time and the number of SQL statements of every request. `playwright/measure/benchmark.js` calls 20 endpoints through the real API (2 warm-up calls, then 30 measured calls, or 5 / 2 for slow endpoints) and keeps the **median**. `playwright/measure/explain.sql` asks MySQL how it runs 8 key queries (`EXPLAIN ANALYZE`). |
| **Fair test** | Same database, same data, same machine and the same JVM settings for both runs. The only difference is the 5 indexes. Each side is measured twice and the second, warm run is used, because the first run after a start is up to 2.5x slower on tiny endpoints (JVM warm-up). Two runs of the **same** code differed by about 5% on the heavy endpoints, so differences smaller than about 10% are noise. |

## The indexes (Step 1)

| Table | Index | Helps |
|---|---|---|
| `tickets` | `(schedule_id, ticket_status)` | "Tickets of this showtime that are not cancelled": seat map, booking check, seat stats, revenue reports, the status refresh. |
| `tickets` | `(booked_time)` | "Newest bookings first" without sorting every ticket. |
| `schedule` | `(movie_id, show_date)` | Upcoming showtimes of one movie. |
| `schedule` | `(cinema_id, show_date, slot)` | "Is this cinema, day and slot already taken?" when adding a schedule. |
| `seat_price` | `(schedule_id, seat_category)` | The price of one seat category in one showtime. |

MySQL already had indexes on `ticket_code` (unique) and on every foreign key. When a new index starts with the same column, MySQL drops the older single-column one automatically, which is why the new indexes cost less space than five brand-new ones would.

## Result 1: what MySQL does (EXPLAIN ANALYZE, one query)

| Query | Before | After | What changed |
|---|---|---|---|
| Taken seats of a showtime | 0.078 ms | 0.077 ms | Nothing: the foreign-key index already did the job. |
| Upcoming showtimes of a movie | 0.066 ms, reads 47 rows | 0.043 ms, reads 17 rows | Index range scan on movie + date. |
| Is this cinema/day/slot taken? | 0.319 ms, reads 350 rows | 0.016 ms, reads 1 row | **20x**: used to read every showtime of the cinema. |
| Price of a seat category | 0.015 ms | 0.013 ms | Nothing noticeable. |
| **Newest 20 bookings** | **74.7 ms**: read all 280,000 tickets and sort them | **0.047 ms**: read 20 rows from the index | **about 1,600x**. |
| Seat stats (tickets per showtime) | 98.8 ms | 61.2 ms | 38% less: the index alone answers the question (covering index). |
| Revenue per movie (leaderboard) | 1,777 ms | 1,440 ms | 19% less: better joins, but it still joins 255,000 tickets. |
| Status refresh, tickets part | 19 rows per showtime | 10 rows per showtime | The status check now happens inside the index. The `schedule` table is still read in full (14,352 rows). |

## Result 2: what the API does (median of the measured calls)

| Endpoint | Before | After | Change | SQL statements per request |
|---|---|---|---|---|
| Now showing | 4.2 ms | 4.1 ms | same | 1 |
| Coming soon | 10.5 ms | 10.1 ms | same | 1 |
| Movie showtimes (47 showtimes) | 2.6 ms | 2.6 ms | same | 1 |
| One schedule | 2.4 ms | 2.4 ms | same | 1 |
| Seat map | 7.1 ms | 6.6 ms | same | 4 |
| Occupied seats | 1.7 ms | 2.1 ms | same (noise) | 1 |
| Book one seat | 12 ms | 13.1 ms | same | 9 |
| Add schedule (overlap check, rejected) | 8 ms | 7.9 ms | same | 4 |
| My tickets (user with about 93) | 4 ms | 4.3 ms | same | 2 |
| Seat stats (admin) | 254.8 ms | 196.6 ms | 23% less | 1 |
| Best showtimes (admin) | 1.28 s | 993 ms | 23% less | 1 |
| Average revenue (admin) | 1.63 s | 1.19 s | 27% less | 1 |
| Movie table (admin) | 2.16 s | 1.84 s | 15% less | 1 |
| Movie leaderboard (admin) | 1.50 s | 1.25 s | 17% less | 1 |
| Total sales (admin) | 1.20 s | 918 ms | 24% less | 2 |
| Total bookings (admin) | 53.7 ms | 47.8 ms | same | 2 |
| Revenue today (admin) | 389.6 ms | 359.8 ms | same | 2 |
| Users table (admin) | 2.42 s | 1.99 s | 18% less | 3 |
| Weekly sales trend (admin) | 1.66 s | 1.73 s | same | 2 |
| All bookings (admin) | 1.89 s | 1.93 s | same | 2 |
| **Status refresh job (runs every minute)** | **385 ms** (avg of 5) | **99 ms** (avg of 4) | **about 4x faster** | n/a |

## Cost of the indexes

| | Without the new indexes | With them |
|---|---|---|
| Inserting 280,000 tickets (3 rounds) | about 5.0 s | about 6.1 s (**about 20% slower**) |
| Size of the `tickets` indexes | 29.1 MB | 40.6 MB (**+11.5 MB**; the table data is 16.5 MB either way) |

One booking inserts a handful of rows, so the write cost is not noticeable per request. It only shows in bulk loads.

## What this tells us

1. **Everyday endpoints did not change, because they were already fast.** The seat map, booking and movie showtimes take 2 to 13 ms. MySQL had already indexed the foreign keys, so these lookups were never scanning tables. Indexes do not make fast things faster.
2. **The once-a-minute status refresh got about 4x faster.** It runs 1,440 times a day, so it is the one background cost that adds up.
3. **The admin analytics endpoints take 15 to 27% less time**, but they still take 0.9 to 2.4 seconds. They add up 255,000 tickets on every call, and no index changes that. They need a different fix: caching (Phase 4, step 4).
4. **The newest-bookings query went from 75 ms to under 0.1 ms**, but the app does not use it yet. The bookings endpoint still loads every ticket (1.9 s). The weekly sales trend (1.7 s) does the same. Neither moved, as predicted. They are the targets of the next steps: the weekly trend in SQL and pagination.
5. **Request counts are already low.** No endpoint runs more than 9 statements, which confirms the phase 3 work. Booking one seat takes 9 statements, which includes the two lock queries (schedule and user).

## Limits of these numbers

- The data is generated and evenly spread. Real data is lumpier.
- The database is local. On Render, with the database elsewhere, **every statement also pays network time**, so the savings from fewer statements (phases 2 and 3) matter more there than any index. These numbers cover only the work done on the server and in MySQL.
- One request at a time. They say nothing about many users at once.
- The slow endpoints were measured 5 times (2 for the slowest), enough to compare 20% differences, not 5% ones.

## Reproduce

```bash
# 1. a throwaway MySQL on port 3307 with an empty mjcinema_test database, the app started once so Hibernate creates the tables
# 2. register users "admin1" and "bench" (password pass1234), then fill the database:
mysql --port=3307 --user=root mjcinema_test < playwright/measure/seed-big.sql
# 3. start the app with  MJCINEMA_TIMING_COUNT_QUERIES=true  and run:
cd playwright
node measure/benchmark.js before <app-log> before.json
mysql ... mjcinema_test -e "source measure/explain.sql"
# 4. add the indexes (rebuild and restart the app: Hibernate creates them), then repeat and compare:
node measure/benchmark.js after <app-log> after.json
node measure/compare.js before.json after.json
```
