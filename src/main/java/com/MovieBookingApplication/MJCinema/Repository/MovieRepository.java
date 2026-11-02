package com.MovieBookingApplication.MJCinema.Repository;

import com.MovieBookingApplication.MJCinema.DTO.*;
import com.MovieBookingApplication.MJCinema.Entity.Movie;
import com.MovieBookingApplication.MJCinema.Entity.MovieStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {

    boolean existsByTmdbId(Long tmdbId);

    @Query("SELECT new com.MovieBookingApplication.MJCinema.DTO.ShowMoviePerCinemaResponse(m.poster, m.title, m.rating, m.overview, m.releaseDate, m.movieId, m.status) FROM Movie m")
    List<ShowMoviePerCinemaResponse> findAllMovies();

    List<Movie> findByStatus(MovieStatus status);

    // A "future schedule" is an ACTIVE schedule that has not started yet.
    // The three updates below cover every movie exactly once.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Movie m SET m.status = com.MovieBookingApplication.MJCinema.Entity.MovieStatus.NOW_SHOWING " +
            "WHERE EXISTS (SELECT 1 FROM Schedule s WHERE s.movie = m " +
            "AND s.status = com.MovieBookingApplication.MJCinema.Entity.ScheduleStatus.ACTIVE " +
            "AND (s.showDate > :today OR (s.showDate = :today AND s.startTime > :time)) " +
            "AND s.showDate <= :limit)")
    int markNowShowing(LocalDate today, LocalTime time, LocalDate limit);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Movie m SET m.status = com.MovieBookingApplication.MJCinema.Entity.MovieStatus.COMING_SOON " +
            "WHERE EXISTS (SELECT 1 FROM Schedule s WHERE s.movie = m " +
            "AND s.status = com.MovieBookingApplication.MJCinema.Entity.ScheduleStatus.ACTIVE " +
            "AND (s.showDate > :today OR (s.showDate = :today AND s.startTime > :time))) " +
            "AND NOT EXISTS (SELECT 1 FROM Schedule s2 WHERE s2.movie = m " +
            "AND s2.status = com.MovieBookingApplication.MJCinema.Entity.ScheduleStatus.ACTIVE " +
            "AND (s2.showDate > :today OR (s2.showDate = :today AND s2.startTime > :time)) " +
            "AND s2.showDate <= :limit)")
    int markComingSoon(LocalDate today, LocalTime time, LocalDate limit);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Movie m SET m.status = com.MovieBookingApplication.MJCinema.Entity.MovieStatus.INACTIVE " +
            "WHERE NOT EXISTS (SELECT 1 FROM Schedule s WHERE s.movie = m " +
            "AND s.status = com.MovieBookingApplication.MJCinema.Entity.ScheduleStatus.ACTIVE " +
            "AND (s.showDate > :today OR (s.showDate = :today AND s.startTime > :time)))")
    int markInactive(LocalDate today, LocalTime time);

    // one query for the coming soon list instead of one findNextSchedule per movie.
    @Query("SELECT new com.MovieBookingApplication.MJCinema.DTO.ComingSoonResponse(" +
            "m.poster, m.title, m.rating, m.overview, m.releaseDate, MIN(s.showDate), m.movieId) " +
            "FROM Schedule s JOIN s.movie m " +
            "WHERE m.status = com.MovieBookingApplication.MJCinema.Entity.MovieStatus.COMING_SOON " +
            "AND s.status = com.MovieBookingApplication.MJCinema.Entity.ScheduleStatus.ACTIVE " +
            "AND (s.showDate > :today OR (s.showDate = :today AND s.startTime > :time)) " +
            "GROUP BY m.movieId, m.poster, m.title, m.rating, m.overview, m.releaseDate")
    List<ComingSoonResponse> findComingSoon(LocalDate today, LocalTime time);

    @Query("SELECT new com.MovieBookingApplication.MJCinema.DTO.MovieLeaderboardResponse(m.title, SUM(sp.price)) " +
            "FROM Tickets t " +
            "JOIN t.schedule s " +
            "JOIN s.movie m " +
            "JOIN SeatPrice sp ON sp.seatCategory = t.seat.seatCategory AND sp.schedule = t.schedule " +
            "WHERE t.ticketStatus != com.MovieBookingApplication.MJCinema.Entity.TicketStatus.CANCELLED " +
            "GROUP BY m.title " +
            "ORDER BY SUM(sp.price) DESC LIMIT 3") // Use the function again or the alias if supported
    List<MovieLeaderboardResponse> getMovieRevenueLeaderboard();

    @Query("SELECT new com.MovieBookingApplication.MJCinema.DTO.ShowTotalAndActiveMovies(" +
            "COUNT(m), " +
            "SUM(CASE WHEN m.status = com.MovieBookingApplication.MJCinema.Entity.MovieStatus.INACTIVE THEN 0L ELSE 1L END)) " +
            "FROM Movie m")
    ShowTotalAndActiveMovies getMovieLibraryStats();

    @Query("SELECT new com.MovieBookingApplication.MJCinema.DTO.ShowMovieInTable(" +
            "m.title, " +
            "m.rating, " +
            "m.releaseDate, " +
            "m.status, " +
            "COALESCE(SUM(sp.price), 0.0)) " + // Sum of the actual seat prices
            "FROM Movie m " +
            "LEFT JOIN Schedule s ON s.movie.id = m.id " +
            "LEFT JOIN Tickets t ON t.schedule.id = s.id AND t.ticketStatus != 'CANCELLED' " +
            "LEFT JOIN SeatPrice sp ON sp.seatCategory = t.seat.seatCategory AND sp.schedule = s " +
            "GROUP BY m.id, m.title, m.rating, m.releaseDate, m.status " +
            "ORDER BY COALESCE(SUM(sp.price), 0.0) DESC")
    List<ShowMovieInTable> showMovieTable();

}


