package com.MovieBookingApplication.MJCinema.Controllers;

import com.MovieBookingApplication.MJCinema.DTO.ShowCinemaRequest;
import com.MovieBookingApplication.MJCinema.DTO.ShowMoviePerCinemaResponse;
import com.MovieBookingApplication.MJCinema.Services.CinemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/cinema")
public class CinemaController {


    @Autowired
    private CinemaService cinemaService;
    @GetMapping("/movies/{id}")
    public ResponseEntity<List<ShowMoviePerCinemaResponse>> showMovies(@PathVariable ("id") Integer cinemaId){
        List<ShowMoviePerCinemaResponse> cinemaMovies = cinemaService.findMoviesPerCinema(cinemaId);

        return ResponseEntity.ok(cinemaMovies);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ShowCinemaRequest>> showCinema(){
        List<ShowCinemaRequest> cinemas = cinemaService.showCinemas();

        return ResponseEntity.ok(cinemas);
    }
}
