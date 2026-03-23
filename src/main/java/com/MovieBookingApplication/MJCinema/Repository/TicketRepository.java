package com.MovieBookingApplication.MJCinema.Repository;

import com.MovieBookingApplication.MJCinema.DTO.MovieTicketsDTO;
import com.MovieBookingApplication.MJCinema.DTO.ShowBookingsResponse;
import com.MovieBookingApplication.MJCinema.Entity.TicketStatus;
import com.MovieBookingApplication.MJCinema.Entity.Tickets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Tickets, Integer> {


    @Query("SELECT COUNT(t) > 0 FROM Tickets t " +
            "WHERE t.schedule.scheduleId = :scheduleId " +
            "AND t.seat.seatNumber = :seatNumber " +
            "AND t.ticketStatus != com.MovieBookingApplication.MJCinema.Entity.TicketStatus.CANCELLED")
    boolean existsByScheduleScheduleIdAndSeatSeatNumber(Integer scheduleId, String seatNumber);

    @Query("SELECT new com.MovieBookingApplication.MJCinema.DTO.MovieTicketsDTO(" +
            "t.user.username, t.schedule.movie.title, t.schedule.cinema.location, t.seat.seatNumber, t.seat.seatCategory," +
            "t.ticketCode, t.schedule.showDate, t.schedule.startTime, t.ticketStatus) " +
            "FROM Tickets t "+
            "WHERE t.schedule.movie.movieId = :movieId" //the colon is like a substitute
    )
    List<MovieTicketsDTO> FindTicketsByMovie(Integer movieId);

    @Query("SELECT t.seat.seatNumber FROM Tickets t WHERE t.schedule.scheduleId = :scheduleId")
    List<String> FindSeatSeatNumberByScheduleId(Integer scheduleId);


    @Query("SELECT new com.MovieBookingApplication.MJCinema.DTO.MovieTicketsDTO(" +
            "t.user.username, t.schedule.movie.title, t.schedule.cinema.location, t.seat.seatNumber, t.seat.seatCategory," +
            "t.ticketCode, t.schedule.showDate, t.schedule.startTime, t.ticketStatus) " +
            "FROM Tickets t "+
            "WHERE t.user.username = :username" //the colon is like a substitute
    )
    List<MovieTicketsDTO> FindTicketsByUserUsername(String username);

    @Query("SELECT t from Tickets t WHERE t.ticketCode = :ticketCode")
    Optional<Tickets> findByTicketCode(String ticketCode);

    @Query("SELECT t FROM Tickets t WHERE t.schedule.scheduleId = :scheduleId " +
    "AND t.ticketStatus != com.MovieBookingApplication.MJCinema.Entity.TicketStatus.CANCELLED")
    List<Tickets> findByScheduleScheduleId(Integer scheduleId);


    @Query("SELECT new com.MovieBookingApplication.MJCinema.DTO.MovieTicketsDTO(" +
            "t.user.username, t.schedule.movie.title, t.schedule.cinema.location, t.seat.seatNumber, t.seat.seatCategory," +
            "t.ticketCode, t.schedule.showDate, t.schedule.startTime, t.ticketStatus) " +
            "FROM Tickets t "+
            "WHERE t.user.userId = :userId" //the colon is like a substitute
    )
    List<MovieTicketsDTO> findTicketByUserUserId(Integer userId);

    List<Tickets> findByTicketStatus(TicketStatus ticketStatus);


    List<Tickets> findByUserUsername(String username);

    @Query("SELECT COALESCE(SUM(sp.price), 0.0) " +
            "FROM Tickets t " +
            "JOIN SeatPrice sp ON sp.seatCategory = t.seat.seatCategory " +
            "AND sp.schedule = t.schedule " +
            "WHERE t.ticketStatus != com.MovieBookingApplication.MJCinema.Entity.TicketStatus.CANCELLED")
    Double showTotalSales(); //takes the sum of all tickets.

    @Query("SELECT new com.MovieBookingApplication.MJCinema.DTO.ShowBookingsResponse(" +
            "t.ticketCode, t.user.username, t.seat.seatNumber, t.schedule.showDate, t.schedule.cinema.cinemaName," +
            "t.schedule.startTime, t.ticketStatus) " +
            "FROM Tickets t "+
            "JOIN t.schedule s " +
            "JOIN s.cinema c " +
            "ORDER by t.bookedTime DESC"//the colon is like a substitute
    )
    List<ShowBookingsResponse> showBookings();

    @Query("SELECT t.seat.seatNumber " +
            "FROM Tickets t "+
            "WHERE t.ticketCode = :seatCode" //the colon is like a substitute
    )
    String findSeatNumberByTicketCode(String seatCode);

    @Query("SELECT COUNT(t) " +
            "FROM Tickets t " +
            "WHERE t.ticketStatus != com.MovieBookingApplication.MJCinema.Entity.TicketStatus.CANCELLED")
    Integer showTotalBookings();

    @Query("SELECT COALESCE(SUM(sp.price), 0.0) " +
            "FROM Tickets t " +
            "JOIN SeatPrice sp ON sp.seatCategory = t.seat.seatCategory " +
            "AND sp.schedule = t.schedule " +
            "WHERE t.ticketStatus != com.MovieBookingApplication.MJCinema.Entity.TicketStatus.CANCELLED "
            + "AND FUNCTION('DATE', t.bookedTime) = CURRENT_DATE")
    Double showRevenueToday();
}
