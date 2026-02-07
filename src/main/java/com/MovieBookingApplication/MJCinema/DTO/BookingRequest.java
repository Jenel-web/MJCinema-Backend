package com.MovieBookingApplication.MJCinema.DTO;

import com.MovieBookingApplication.MJCinema.Entity.Seat;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class BookingRequest {
    private Integer userId;
    @NotNull(message = "Schedule Id is required.")
    private Integer scheduleId;
    @NotEmpty(message = "At least one seat must be selected.")
    List<String> selectedSeat;
    public BookingRequest() {
    }
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public List<String> getSelectedSeat() {
        return selectedSeat;
    }

    public void setSelectedSeat(List<String> selectedSeat) {
        this.selectedSeat = selectedSeat;
    }

    public Integer getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Integer scheduleId) {
        this.scheduleId = scheduleId;
    }
}