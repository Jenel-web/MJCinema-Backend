package com.MovieBookingApplication.MJCinema.Repository;

import com.MovieBookingApplication.MJCinema.DTO.ShowCinemaRequest;
import com.MovieBookingApplication.MJCinema.Entity.Cinema;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface CinemaRepository extends JpaRepository<Cinema, Integer>{

    @Query("SELECT new com.MovieBookingApplication.MJCinema.DTO.ShowCinemaRequest(c.location, c.cinemaId) FROM Cinema c")
    List<ShowCinemaRequest> findAllCinemas(); //right way to get something from db
}
