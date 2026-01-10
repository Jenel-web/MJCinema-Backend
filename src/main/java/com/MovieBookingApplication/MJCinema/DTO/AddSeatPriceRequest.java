package com.MovieBookingApplication.MJCinema.DTO;

import jakarta.validation.constraints.NotNull;

public class AddSeatPriceRequest {
    @NotNull(message = "Schedule Id required.")
    public Integer scheduleId;


    public Integer getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Integer scheduleId) {
        this.scheduleId = scheduleId;
    }
}
