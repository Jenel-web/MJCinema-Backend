package com.MovieBookingApplication.MJCinema.DTO;

import com.MovieBookingApplication.MJCinema.Entity.ScheduleStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public class ShowSeatStatsResponse {
    private String cinemaName;
    private LocalDate showDate;
    private ScheduleStatus status;
    private Integer occupiedSeats;
    private Integer availableSeats;

}
