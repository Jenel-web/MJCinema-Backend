package com.MovieBookingApplication.MJCinema.Repository;

import com.MovieBookingApplication.MJCinema.DTO.*;
import com.MovieBookingApplication.MJCinema.Entity.Movie;
import com.MovieBookingApplication.MJCinema.Entity.MovieStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {

    boolean existsByTmdbId(Long tmdbId);

    @Query("SELECT new com.MovieBookingApplication.MJCinema.DTO.ShowMoviePerCinemaResponse(m.poster, m.title, m.rating, m.overview, m.releaseDate, m.movieId, m.status) FROM Movie m")
    List<ShowMoviePerCinemaResponse> findAllMovies();

    List<Movie> findByStatus(MovieStatus status);

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
}


