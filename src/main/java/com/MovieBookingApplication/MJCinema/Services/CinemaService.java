package com.MovieBookingApplication.MJCinema.Services;

import com.MovieBookingApplication.MJCinema.DTO.ShowMoviePerCinemaResponse;
import com.MovieBookingApplication.MJCinema.DTO.ShowingSchedResponse;
import com.MovieBookingApplication.MJCinema.Entity.Cinema;
import com.MovieBookingApplication.MJCinema.Entity.Movie;
import com.MovieBookingApplication.MJCinema.Entity.MovieStatus;
import com.MovieBookingApplication.MJCinema.Entity.Schedule;
import com.MovieBookingApplication.MJCinema.Repository.CinemaRepository;
import com.MovieBookingApplication.MJCinema.Repository.MovieRepository;
import com.MovieBookingApplication.MJCinema.Repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CinemaService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private CinemaRepository cinemaRepository;

    @Autowired
    private MovieRepository movieRepository;

    public List<ShowMoviePerCinemaResponse> findMoviesPerCinema(Integer cinemaId){

        List<Movie> allMovies = movieRepository.findAll();
        LocalTime timeNow = LocalTime.now();
        LocalDate now = LocalDate.now();

        List <ShowMoviePerCinemaResponse> moviesPerCinema = new ArrayList<>();

        for(Movie m : allMovies){
            Optional<Schedule> movieSched = scheduleRepository.findNextSchedulePerCinema(m, now, timeNow, cinemaId);

            if(movieSched.isPresent()){
                Long daysBeforeMovie = ChronoUnit.DAYS.between(now, movieSched.get().getShowDate());

                if(daysBeforeMovie <= 5){
                    m.setStatus(MovieStatus.NOW_SHOWING);

                }
                else{
                    m.setStatus(MovieStatus.COMING_SOON);

                }

                moviesPerCinema.add(new ShowMoviePerCinemaResponse(
                        m.getPoster(), m.getTitle(), m.getRating(),
                        m.getOverview(), m.getReleaseDate(), m.getMovieId(),
                        m.getStatus()
                ));
            }
        }
        return moviesPerCinema;
    }
}
