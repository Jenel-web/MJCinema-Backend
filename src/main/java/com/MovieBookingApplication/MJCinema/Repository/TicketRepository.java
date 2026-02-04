package com.MovieBookingApplication.MJCinema.Repository;

import com.MovieBookingApplication.MJCinema.DTO.MovieTicketsDTO;
import com.MovieBookingApplication.MJCinema.DTO.ShowingSchedResponse;
import com.MovieBookingApplication.MJCinema.Entity.Tickets;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Tickets, Integer> {

    boolean existsByScheduleScheduleIdAndSeatSeatNumber(Integer scheduleId, String seatNumber);

    @Query("SELECT new com.MovieBookingApplication.MJCinema.DTO.MovieTicketsDTO(" +
            "t.user.username, t.schedule.movie.title, t.schedule.cinema.location, t.seat.seatNumber, t.seat.seatCategory," +
            "t.ticketCode, t.schedule.showDate, t.schedule.startTime) " +
            "FROM Tickets t "+
            "WHERE t.schedule.movie.movieId = :movieId" //the colon is like a substitute
    )
    List<MovieTicketsDTO> FindTicketsByMovie(Integer movieId);

    @Query("SELECT t.seat.seatNumber FROM Tickets t WHERE t.schedule.scheduleId = :scheduleId")
    List<String> FindSeatSeatNumberByScheduleId(Integer scheduleId);


    @Query("SELECT new com.MovieBookingApplication.MJCinema.DTO.MovieTicketsDTO(" +
            "t.user.username, t.schedule.movie.title, t.schedule.cinema.location, t.seat.seatNumber, t.seat.seatCategory," +
            "t.ticketCode, t.schedule.showDate, t.schedule.startTime) " +
            "FROM Tickets t "+
            "WHERE t.user.username = :username" //the colon is like a substitute
    )
    List<MovieTicketsDTO> FindTicketsByUserUsername(String username);

    @Query("SELECT t from Tickets t WHERE t.ticketCode = :ticketCode")
    Optional<Tickets> findByTicketCode(String ticketCode);

    List<Tickets> findByScheduleScheduleId(Integer scheduleId);



}
