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
    private Double percentage;
    private ScheduleSlot slot;
    private String title;

    public ShowSeatStatsResponse(String title,
                                 String cinemaName,
                                 LocalDate showDate,
                                 ScheduleStatus status,
                                 Integer occupiedSeats,
                                 Integer availableSeats,
                                 Double percentage,
                                 ScheduleSlot slot) {
        this.title = title;
        this.cinemaName = cinemaName;
        this.showDate = showDate;
        this.status = status;
        this.occupiedSeats = occupiedSeats;
        this.availableSeats = availableSeats;
        this.percentage = percentage;
        this.slot = slot;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ScheduleSlot getSlot() {
        return slot;
    }

    public void setSlot(ScheduleSlot slot) {
        this.slot = slot;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public LocalDate getShowDate() {
        return showDate;
    }

    public void setShowDate(LocalDate showDate) {
        this.showDate = showDate;
    }

    public ScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduleStatus status) {
        this.status = status;
    }

    public Integer getOccupiedSeats() {
        return occupiedSeats;
    }

    public void setOccupiedSeats(Integer occupiedSeats) {
        this.occupiedSeats = occupiedSeats;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }
}
