package com.MovieBookingApplication.MJCinema.Repository;

import com.MovieBookingApplication.MJCinema.DTO.MovieDetailsDTO;
import com.MovieBookingApplication.MJCinema.DTO.MovieTicketsDTO;
import com.MovieBookingApplication.MJCinema.DTO.ShowMoviePerCinemaResponse;
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
}
