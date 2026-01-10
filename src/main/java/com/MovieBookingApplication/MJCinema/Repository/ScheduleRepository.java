package com.MovieBookingApplication.MJCinema.Repository;

import com.MovieBookingApplication.MJCinema.DTO.ScheduleSlot;
import com.MovieBookingApplication.MJCinema.DTO.ShowingSchedResponse;
import com.MovieBookingApplication.MJCinema.Entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
}
