package com.MovieBookingApplication.MJCinema.Repository;

import com.MovieBookingApplication.MJCinema.DTO.*;
import com.MovieBookingApplication.MJCinema.Entity.Movie;
import com.MovieBookingApplication.MJCinema.Entity.MovieStatus;
import com.MovieBookingApplication.MJCinema.Entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {

    @Query("SELECT new com.MovieBookingApplication.MJCinema.DTO.ShowingSchedResponse(" +
            "m.title, s.showDate, s.cinema.location, s.startTime) " +
            "FROM Schedule s JOIN s.movie m " +
            "WHERE s.showDate >= CURRENT_DATE")
     List<ShowingSchedResponse> FindNowShowingSchedules();


    @Query("SELECT COUNT(s) > 0 FROM Schedule s WHERE s.cinema.id = :cinemaId AND s.showDate = :showDate " +
            "AND s.slot = :slot" )
    boolean existsOverlappingSchedule(@Param("cinemaId") Integer cinemaId,
                                      @Param("showDate")LocalDate showDate,
                                      @Param("slot") ScheduleSlot slot);

    @Query("SELECT s FROM Schedule s WHERE s.movie = :movie AND (s.showDate > :today OR (s.showDate = :today AND s.startTime > :time)) ORDER BY s.showDate ASC LIMIT 1")
    Optional<Schedule> findNextSchedule(Movie movie, LocalDate today, LocalTime time); //makes db do the heavy lifting

    @Query("SELECT s FROM Schedule s WHERE s.cinema.cinemaId = :id AND s.movie = :movie AND (s.showDate > :today OR (s.showDate = :today AND s.startTime > :time)) ORDER BY s.showDate ASC LIMIT 1")
    Optional<Schedule> findNextSchedulePerCinema(Movie movie, LocalDate today, LocalTime time, Integer id); //makes db do the heavy lifting

    @Query ("SELECT s FROM Schedule s WHERE s.showDate < :today OR (s.showDate = :today AND s.startTime < :time)")
    List<Schedule> findDoneSchedules(LocalDate today, LocalTime time);

    @Query("SELECT s FROM Schedule s WHERE s.movie.movieId = :movieId AND (s.showDate > :today OR (s.showDate = :today AND s.startTime > :time))")
    List<Schedule> findByMovieId(Integer movieId, LocalDate today, LocalTime time);

    @Query("SELECT s FROM Schedule s WHERE s.status = com.MovieBookingApplication.MJCinema.Entity.ScheduleStatus.ACTIVE")
    List<Schedule> ShowActiveSchedules();

    @Query("SELECT new com.MovieBookingApplication.MJCinema.DTO.BestShowTimeResponse(" +
            "s.slot, " +
            "COALESCE(SUM(sp.price) / NULLIF(COUNT(DISTINCT s.id), 0), 0.0)) " +
            "FROM Schedule s " +
            "LEFT JOIN Tickets t ON t.schedule.id = s.id AND t.ticketStatus != 'CANCELLED' " +
            "LEFT JOIN SeatPrice sp ON sp.seatCategory = t.seat.seatCategory AND sp.schedule = s " +
            "WHERE t.ticketStatus != com.MovieBookingApplication.MJCinema.Entity.TicketStatus.CANCELLED " +
            "GROUP BY s.slot " +
            "ORDER BY COALESCE(SUM(sp.price) / NULLIF(COUNT(DISTINCT s.id), 0), 0.0) DESC LIMIT 3")
    List<BestShowTimeResponse> getAverageRevenuePerSlot();
}
