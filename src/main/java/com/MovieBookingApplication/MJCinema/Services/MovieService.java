package com.MovieBookingApplication.MJCinema.Services;

import com.MovieBookingApplication.MJCinema.DTO.MovieDetailsDTO;
import com.MovieBookingApplication.MJCinema.DTO.TmdbMovieDTO;
import com.MovieBookingApplication.MJCinema.DTO.TmdbMovieResponse;
import com.MovieBookingApplication.MJCinema.Entity.Movie;
import com.MovieBookingApplication.MJCinema.Entity.MovieStatus;
import com.MovieBookingApplication.MJCinema.Repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TmdbService tmdbService; //needed for fetching a movie.
    public boolean addMovie(Long tmdbId){
        try {
            TmdbMovieDTO tmdbMovieDTO = tmdbService.getMovieDetails(tmdbId);

            if(movieRepository.existsByTmdbId(tmdbId)){
                throw new RuntimeException("Movie already added.");
            }

            if (tmdbMovieDTO == null) return false;
            Movie movie = new Movie();

            movie.setTmdbId(tmdbMovieDTO.getId());
            movie.setOverview(tmdbMovieDTO.getOverview());
            movie.setPoster(tmdbMovieDTO.getPosterPath());
            movie.setReleaseDate(LocalDate.parse(tmdbMovieDTO.getRelease_date())); //release date is string in tmdb
            movie.setTitle(tmdbMovieDTO.getTitle());
            movie.setDuration("2 hrs");
            movie.setStatus(MovieStatus.NEW);
            movie.setRating(tmdbMovieDTO.getVote_average());
            movieRepository.save(movie);

            //remember that every field is nullable so make sure that every field is filled before saving to repo.
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<MovieDetailsDTO> showMovies(){
        return movieRepository.findAllMovies();
    }
}
