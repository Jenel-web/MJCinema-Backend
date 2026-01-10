package com.MovieBookingApplication.MJCinema.DTO;

import jakarta.validation.constraints.NotNull;

public class BookingRequest {
    @NotNull(message = "Schedule Id is required.")
    private Integer scheduleId;
    @NotNull(message = "Seat Number is required.")
    private String seat;

    public Integer getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Integer scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getSeat() {
        return seat;
    }

    public void setSeat(String seat) {
        this.seat = seat;
    }
}
