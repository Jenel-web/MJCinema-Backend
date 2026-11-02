package com.MovieBookingApplication.MJCinema.DTO;

import com.MovieBookingApplication.MJCinema.Entity.SeatCategory;

public record SeatPriceResponse(SeatCategory seatCategory, Double price) {
}
