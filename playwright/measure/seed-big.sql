-- A big, deterministic data set for measuring (run against the throwaway test database only).
-- 40 cinemas x 140 seats, 300 movies, 14,000 showtimes (about a third in the future),
-- 42,000 seat prices, about 280,000 tickets and 3,000 users. Users 1 (admin1) and 2 (bench) must already exist.
SET SESSION cte_max_recursion_depth = 400000;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE tickets; TRUNCATE seat_price; TRUNCATE schedule; TRUNCATE seat; TRUNCATE movie; TRUNCATE cinema;
DELETE FROM users WHERE user_id > 2;
SET FOREIGN_KEY_CHECKS = 1;

UPDATE users SET role = 'ADMIN' WHERE user_id = 1;
UPDATE users SET role = 'USER', balance = 1000000000 WHERE user_id = 2;

-- cinemas and seats (seat_id = (cinema - 1) * 140 + k, rows A-J with 14 seats each; A-B VIP, G-H BALCONY, rest REGULAR)
INSERT INTO cinema (cinema_id, location, cinema_name, total_rows, total_columns, total_seats, seat_layout)
WITH RECURSIVE n(i) AS (SELECT 1 UNION ALL SELECT i + 1 FROM n WHERE i < 40)
SELECT i, CONCAT('Mall ', i), CONCAT('Cinema ', i), 10, 16, 140, 'SSSS_SSSSSS_SSSS' FROM n;

INSERT INTO seat (seat_id, cinema_id, seat_category, seat_number)
WITH RECURSIVE c(i) AS (SELECT 1 UNION ALL SELECT i + 1 FROM c WHERE i < 40),
               k(j) AS (SELECT 1 UNION ALL SELECT j + 1 FROM k WHERE j < 140)
SELECT (c.i - 1) * 140 + k.j, c.i,
       CASE WHEN CEIL(k.j / 14) <= 2 THEN 'VIP' WHEN CEIL(k.j / 14) IN (7, 8) THEN 'BALCONY' ELSE 'REGULAR' END,
       CONCAT(CHAR(64 + CEIL(k.j / 14)), ((k.j - 1) MOD 14) + 1)
FROM c CROSS JOIN k;

INSERT INTO movie (movie_id, tmdb_id, poster, title, duration, rating, overview, status, release_date)
WITH RECURSIVE n(i) AS (SELECT 1 UNION ALL SELECT i + 1 FROM n WHERE i < 300)
SELECT i, i, CONCAT('/p', i, '.jpg'), CONCAT('Movie ', i), '2 hrs', 5 + (i MOD 5), 'A made-up movie for measuring.', 'INACTIVE', '2026-01-01' FROM n;

-- schedules: cinema, slot and day are spread so no two showtimes share a cinema + day + slot
INSERT INTO schedule (schedule_id, movie_id, cinema_id, show_date, slot, start_time, end_time, status)
WITH RECURSIVE n(i) AS (SELECT 0 UNION ALL SELECT i + 1 FROM n WHERE i < 13999)
SELECT i + 1,
       1 + (i MOD 300),
       1 + (i MOD 40),
       CURDATE() + INTERVAL (FLOOR(i / 200) - 45) DAY,
       ELT(1 + (FLOOR(i / 40) MOD 5), 'MORNING', 'MIDDAY', 'AFTERNOON', 'EVENING', 'NIGHT'),
       ELT(1 + (FLOOR(i / 40) MOD 5), '09:00:00', '11:30:00', '14:00:00', '16:30:00', '19:00:00'),
       ELT(1 + (FLOOR(i / 40) MOD 5), '11:00:00', '13:30:00', '16:00:00', '18:30:00', '21:00:00'),
       CASE WHEN FLOOR(i / 200) - 45 < 0 THEN 'COMPLETED'
            WHEN i MOD 50 = 0 THEN 'CANCELLED'
            ELSE 'ACTIVE' END
FROM n;

INSERT INTO seat_price (schedule_id, seat_category, price)
SELECT s.schedule_id, c.cat, c.price
FROM schedule s
CROSS JOIN (SELECT 'VIP' AS cat, 450 AS price UNION ALL SELECT 'REGULAR', 320 UNION ALL SELECT 'BALCONY', 260) c;

-- users (same password hash as admin1, so any of them can log in with "pass1234")
INSERT INTO users (user_id, username, password, role, balance)
WITH RECURSIVE n(i) AS (SELECT 1 UNION ALL SELECT i + 1 FROM n WHERE i < 3000)
SELECT 2 + i, CONCAT('user', i), (SELECT password FROM (SELECT password FROM users WHERE user_id = 1) p), 'USER', 1000 FROM n;

-- tickets: about one seat in seven is taken in every showtime (about 20 of 140)
INSERT INTO tickets (user_id, schedule_id, seat_id, ticket_code, booked_time, ticket_status)
WITH RECURSIVE k(j) AS (SELECT 1 UNION ALL SELECT j + 1 FROM k WHERE j < 140)
SELECT 3 + ((s.schedule_id * 31 + k.j * 17) MOD 3000),
       s.schedule_id,
       (s.cinema_id - 1) * 140 + k.j,
       CONCAT('B-', s.schedule_id, '-', k.j),
       TIMESTAMP(s.show_date, '12:00:00') - INTERVAL ((s.schedule_id + k.j) MOD 10 + 1) DAY + INTERVAL ((s.schedule_id * k.j) MOD 720) MINUTE,
       CASE WHEN s.status = 'CANCELLED' THEN 'CANCELLED'
            WHEN (s.schedule_id + k.j) MOD 12 = 0 THEN 'CANCELLED'
            WHEN s.status = 'COMPLETED' THEN 'COMPLETED'
            ELSE 'ACTIVE' END
FROM schedule s
JOIN k ON (k.j * 37 + s.schedule_id * 11) MOD 7 = 0;

ANALYZE TABLE tickets, schedule, seat_price, seat, movie, cinema, users;
SELECT 'tickets' AS tbl, COUNT(*) AS n FROM tickets
UNION ALL SELECT 'schedule', COUNT(*) FROM schedule
UNION ALL SELECT 'seat_price', COUNT(*) FROM seat_price
UNION ALL SELECT 'users', COUNT(*) FROM users;
