package com.MovieBookingApplication.MJCinema.DTO;

import com.MovieBookingApplication.MJCinema.Entity.Seat;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class BookingRequest {
    @NotNull(message = "Schedule Id is required.")
    private Integer scheduleId;
    @NotNull(message = "Seat Number is required.")
    List<String> selectedSeat;

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