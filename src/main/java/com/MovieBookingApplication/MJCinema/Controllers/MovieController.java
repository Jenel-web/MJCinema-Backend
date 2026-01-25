package com.MovieBookingApplication.MJCinema.Controllers;

import com.MovieBookingApplication.MJCinema.DTO.AddMovieRequest;
import com.MovieBookingApplication.MJCinema.DTO.MovieDetailsDTO;
import com.MovieBookingApplication.MJCinema.Services.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/movie")
public class MovieController {

    private final MovieService movieService;
    public MovieController(MovieService movieService){
        this.movieService = movieService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<String> addMovie (@RequestBody AddMovieRequest request){
        if (request.getId() == null) {
            throw new IllegalArgumentException("TMDB ID cannot be null");
        }
        if(movieService.addMovie(request.getId())){
            return ResponseEntity.ok("Movie saved successfully.");
        }
        else{
            return ResponseEntity.badRequest().body("Movie not saved.");
            //in case it's a bad request.
        }
    }

    @GetMapping("/show")
    public ResponseEntity<List<MovieDetailsDTO>> showMovies(){
        List<MovieDetailsDTO> movieList = movieService.showMovies();

        return ResponseEntity.ok(movieList);
    }
}
