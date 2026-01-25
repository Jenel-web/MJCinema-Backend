package com.MovieBookingApplication.MJCinema.Controllers;

import com.MovieBookingApplication.MJCinema.DTO.TmdbMovieDTO;
import com.MovieBookingApplication.MJCinema.Services.TmdbService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/movie/tmdb")
@CrossOrigin("*") //allows everyone to access, will make it easier for frontend
public class TmdbController {

    private final TmdbService tmdbService;

    public TmdbController (TmdbService tmdbService){
        this.tmdbService = tmdbService;
    }//constructor injection of tmdb service

    @GetMapping("/search")
    public ResponseEntity<List<TmdbMovieDTO>> search(@RequestParam ("name") String movieName){
        //for when they search the name of the movie

        return ResponseEntity.ok(tmdbService.searchMovies(movieName));
    }//this acts like a true search because it outputs all movies with the keyword.
    @GetMapping("/details")
    public ResponseEntity<TmdbMovieDTO> getMovieDetails(@RequestBody Long tmdbId){
        return ResponseEntity.ok(tmdbService.getMovieDetails(tmdbId));

    }
    @GetMapping("/popular")
    public ResponseEntity<List<TmdbMovieDTO>> popular(){//make sure to return the right return type
        return ResponseEntity.ok(tmdbService.getPopularMovies());
    }//returns the list of popular movies
}
