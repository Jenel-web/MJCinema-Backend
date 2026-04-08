package com.MovieBookingApplication.MJCinema.Controllers;

import com.MovieBookingApplication.MJCinema.DTO.*;
import com.MovieBookingApplication.MJCinema.Services.MovieService;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "https://mjcinema-5923l8kkj-jenel-webs-projects.vercel.app")
@RestController
@RequestMapping("/movie")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<String> addMovie(@RequestBody AddMovieRequest request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("TMDB ID cannot be null");
        }
        if (movieService.addMovie(request.getId())) {
            return ResponseEntity.ok("Movie saved successfully.");
        } else {
            return ResponseEntity.badRequest().body("Movie not saved.");
            //in case it's a bad request.
        }
    }

    @GetMapping("/show")
    public ResponseEntity<List<ShowMoviePerCinemaResponse>> showMovies() {
        List<ShowMoviePerCinemaResponse> movieList = movieService.showMovies();

        return ResponseEntity.ok(movieList);
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<MovieLeaderboardResponse>> showMovieLeaderBoard(){
        List<MovieLeaderboardResponse> leaderboard = movieService.showMovieLeaderboard();

        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/showActiveCount")
    public ResponseEntity<ShowTotalAndActiveMovies> showActiveCount(){
        ShowTotalAndActiveMovies stats = movieService.showActiveCount();

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/showMovieTable")
    public ResponseEntity<List<ShowMovieInTable>> showMovieTable(){
        List<ShowMovieInTable> showMovieTable = movieService.showMovieTable();

        return ResponseEntity.ok(showMovieTable);
    }

}