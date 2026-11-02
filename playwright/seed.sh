#!/bin/sh
# Resets the throwaway test database (port 3307) and seeds one cinema, 16 seats, 3 movies and an admin user.
# The app must already be running on localhost:8080 against that database.
M="/c/mysql-client/bin/mysql.exe --host=127.0.0.1 --port=3307 --user=root mjcinema_test"

$M -e "SET FOREIGN_KEY_CHECKS=0; TRUNCATE tickets; TRUNCATE seat_price; TRUNCATE schedule; TRUNCATE seat; TRUNCATE movie; TRUNCATE cinema; TRUNCATE users; SET FOREIGN_KEY_CHECKS=1;"

$M -e "
INSERT INTO cinema (location, cinema_name, total_rows, total_columns, total_seats, seat_layout) VALUES ('Test Mall', 'Cinema 1', 4, 5, 16, 'SS_SS');
INSERT INTO movie (tmdb_id, poster, title, duration, rating, overview, status, release_date) VALUES
 (1, '/a.jpg', 'Alpha Movie', '2 hrs', 7.5, 'First test movie', 'INACTIVE', '2026-01-01'),
 (2, '/b.jpg', 'Beta Movie', '2 hrs', 8.1, 'Second test movie', 'INACTIVE', '2026-01-02'),
 (3, '/c.jpg', 'Gamma Movie', '2 hrs', 6.9, 'Third test movie', 'INACTIVE', '2026-01-03');
"

# seats A1-A4 and B1-B4 are VIP, row C is REGULAR, row D is BALCONY
for row in A B C D; do
  case $row in A|B) cat=VIP;; C) cat=REGULAR;; D) cat=BALCONY;; esac
  for n in 1 2 3 4; do
    $M -e "INSERT INTO seat (cinema_id, seat_category, seat_number) VALUES (1, '$cat', '$row$n');"
  done
done

curl -s -X POST localhost:8080/user/register -H 'Content-Type: application/json' \
  -d '{"username":"admin1","password":"pass1234","confirm":"pass1234"}' > /dev/null
$M -e "UPDATE users SET role='ADMIN' WHERE username='admin1';"
echo "seeded"
