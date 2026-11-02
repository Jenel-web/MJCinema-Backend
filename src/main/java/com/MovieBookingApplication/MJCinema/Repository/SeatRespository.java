package com.MovieBookingApplication.MJCinema.Repository;

import com.MovieBookingApplication.MJCinema.Entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRespository extends JpaRepository<Seat, Integer> {

    Optional <Seat> findBySeatNumberAndCinemaCinemaId(String seatNumber, Integer cinemaId);

    //all the requested seats of a cinema in one query.
    List<Seat> findByCinemaCinemaIdAndSeatNumberIn(Integer cinemaId, Collection<String> seatNumbers);

    @Query("SELECT s FROM Seat s WHERE s.cinema.cinemaId = " +
            "(SELECT sch.cinema.cinemaId FROM Schedule sch WHERE sch.scheduleId= :scheduleId)")
    //finds a seat with the cinema id and also finds a schedule with the same scheduleId as the cinema
    List<Seat> findByScheduleId(Integer scheduleId);



}
