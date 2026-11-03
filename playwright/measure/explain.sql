-- How MySQL answers the queries the app runs (EXPLAIN ANALYZE runs them and reports real time and rows).
-- Run before and after adding indexes and compare:  mysql ... mjcinema_test -e "source explain.sql"

SELECT '1. taken seats of one showtime (booking, seat map)' AS query;
EXPLAIN ANALYZE SELECT st.seat_number FROM tickets t JOIN seat st ON st.seat_id = t.seat_id
 WHERE t.schedule_id = 12345 AND t.ticket_status <> 'CANCELLED';

SELECT '2. upcoming showtimes of one movie (movie details)' AS query;
EXPLAIN ANALYZE SELECT * FROM schedule
 WHERE movie_id = 77 AND status = 'ACTIVE' AND (show_date > CURDATE() OR (show_date = CURDATE() AND start_time > CURTIME()));

SELECT '3. is this cinema + day + slot already taken? (adding a schedule)' AS query;
EXPLAIN ANALYZE SELECT COUNT(*) FROM schedule
 WHERE cinema_id = 5 AND show_date = CURDATE() + INTERVAL 10 DAY AND slot = 'EVENING' AND status <> 'CANCELLED';

SELECT '4. price of one seat category in a showtime (booking)' AS query;
EXPLAIN ANALYZE SELECT * FROM seat_price WHERE schedule_id = 12345 AND seat_category = 'VIP';

SELECT '5. newest 20 bookings (admin bookings page, once paginated)' AS query;
EXPLAIN ANALYZE SELECT t.ticket_code FROM tickets t ORDER BY t.booked_time DESC LIMIT 20;

SELECT '6. seat stats: tickets counted per active showtime' AS query;
EXPLAIN ANALYZE SELECT s.schedule_id, COUNT(t.ticket_id) FROM schedule s
 LEFT JOIN tickets t ON t.schedule_id = s.schedule_id AND t.ticket_status <> 'CANCELLED'
 WHERE s.status = 'ACTIVE' GROUP BY s.schedule_id;

SELECT '7. revenue per movie (leaderboard)' AS query;
EXPLAIN ANALYZE SELECT m.title, SUM(sp.price) FROM tickets t
 JOIN schedule s ON s.schedule_id = t.schedule_id JOIN movie m ON m.movie_id = s.movie_id
 JOIN seat st ON st.seat_id = t.seat_id
 JOIN seat_price sp ON sp.schedule_id = t.schedule_id AND sp.seat_category = st.seat_category
 WHERE t.ticket_status <> 'CANCELLED' GROUP BY m.title ORDER BY SUM(sp.price) DESC LIMIT 3;

SELECT '8. the once-a-minute job: complete tickets of finished showtimes (plan only)' AS query;
EXPLAIN UPDATE tickets t SET t.ticket_status = 'COMPLETED'
 WHERE t.ticket_status = 'ACTIVE'
   AND t.schedule_id IN (SELECT s.schedule_id FROM schedule s WHERE s.show_date < CURDATE() OR (s.show_date = CURDATE() AND s.end_time < CURTIME()));
