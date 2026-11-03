package com.MovieBookingApplication.MJCinema.Repository;

import com.MovieBookingApplication.MJCinema.DTO.MovieTicketsDTO;
import com.MovieBookingApplication.MJCinema.DTO.ShowBookingsResponse;
import com.MovieBookingApplication.MJCinema.DTO.WeeklySales;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.MovieBookingApplication.MJCinema.Entity.TicketStatus;
import com.MovieBookingApplication.MJCinema.Entity.Tickets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
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

    //seats that are taken in a schedule. cancelled tickets free their seat again.
    @Query("SELECT t.seat.seatNumber FROM Tickets t WHERE t.schedule.scheduleId = :scheduleId " +
            "AND t.ticketStatus <> com.MovieBookingApplication.MJCinema.Entity.TicketStatus.CANCELLED")
    List<String> FindSeatSeatNumberByScheduleId(Integer scheduleId);

    //which of the requested seats are already taken, in one query.
    @Query("SELECT t.seat.seatNumber FROM Tickets t WHERE t.schedule.scheduleId = :scheduleId " +
            "AND t.seat.seatNumber IN :seatNumbers " +
            "AND t.ticketStatus <> com.MovieBookingApplication.MJCinema.Entity.TicketStatus.CANCELLED")
    List<String> findTakenSeatNumbers(Integer scheduleId, Collection<String> seatNumbers);

    //tickets of a schedule with their user and seat loaded in the same query.
    @Query("SELECT t FROM Tickets t JOIN FETCH t.user JOIN FETCH t.seat WHERE t.schedule.scheduleId = :scheduleId " +
            "AND t.ticketStatus <> com.MovieBookingApplication.MJCinema.Entity.TicketStatus.CANCELLED")
    List<Tickets> findActiveWithUserAndSeat(Integer scheduleId);


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

    //one bulk update instead of loading every active ticket and its schedule.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE Tickets t SET t.ticketStatus = com.MovieBookingApplication.MJCinema.Entity.TicketStatus.COMPLETED " +
            "WHERE t.ticketStatus = com.MovieBookingApplication.MJCinema.Entity.TicketStatus.ACTIVE " +
            "AND t.schedule IN (SELECT s FROM Schedule s WHERE s.showDate < :today " +
            "OR (s.showDate = :today AND s.endTime < :time))")
    int completePastTickets(LocalDate today, LocalTime time);


    List<Tickets> findByUserUsername(String username);

    @Query("SELECT COALESCE(SUM(sp.price), 0.0) " +
            "FROM Tickets t " +
            "JOIN SeatPrice sp ON sp.seatCategory = t.seat.seatCategory " +
            "AND sp.schedule = t.schedule " +
            "WHERE t.ticketStatus != com.MovieBookingApplication.MJCinema.Entity.TicketStatus.CANCELLED")
    Double showTotalSales(); //takes the sum of all tickets.

    //bookings are read in two steps, newest first:
    //1. findBookingIds picks the ids of one page. Only ids are selected, so MySQL answers it from the booked_time
    //   index alone and never has to join or sort the whole table (a single query with all the joins made MySQL
    //   switch to reading and sorting every ticket as soon as the page number grew: 180 ms at page 100).
    //2. showBookingsByIds loads the details of just those few tickets.
    //ticketId is the tie-breaker: without it two tickets booked in the same instant could swap places between two
    //requests, so a booking could show up twice or never across pages. Spring adds the total-count query itself.
    @Query("SELECT t.ticketId FROM Tickets t ORDER BY t.bookedTime DESC, t.ticketId DESC")
    Page<Integer> findBookingIds(Pageable pageable);

    //same sort as findBookingIds, so the rows come back in the order of the page
    @Query("SELECT new com.MovieBookingApplication.MJCinema.DTO.ShowBookingsResponse(" +
            "t.ticketCode, t.user.username, t.seat.seatNumber, s.showDate, c.cinemaName, " +
            "s.startTime, t.ticketStatus) " +
            "FROM Tickets t " +
            "JOIN t.schedule s " +
            "JOIN s.cinema c " +
            "WHERE t.ticketId IN :ids " +
            "ORDER BY t.bookedTime DESC, t.ticketId DESC")
    List<ShowBookingsResponse> showBookingsByIds(List<Integer> ids);

    //tickets booked per week (the week starts on Monday and is taken from the showtime date), cancelled ones left out.
    //the database does the grouping and counting, so only one row per week comes back to Java.
    //WEEKDAY() is 0 for Monday ... 6 for Sunday, so subtracting it from the date gives that week's Monday.
    @Query(value = "SELECT DATE_FORMAT(DATE_SUB(s.show_date, INTERVAL WEEKDAY(s.show_date) DAY), '%Y-%m-%d') AS weekStart, " +
            "COUNT(*) AS bookings " +
            "FROM tickets t " +
            "JOIN schedule s ON s.schedule_id = t.schedule_id " +
            "WHERE t.ticket_status <> 'CANCELLED' " +
            "GROUP BY weekStart " +
            "ORDER BY weekStart",
            nativeQuery = true)
    List<WeeklySales> weeklySales();

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
