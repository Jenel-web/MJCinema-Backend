package com.MovieBookingApplication.MJCinema.DTO;

import com.MovieBookingApplication.MJCinema.Entity.SeatCategory;

import java.time.LocalDate;

public class SeatPriceDTO {
    private ScheduleSlot slot;
    private LocalDate showDate;
    private SeatCategory seatCategory;
    private Double price;
}
