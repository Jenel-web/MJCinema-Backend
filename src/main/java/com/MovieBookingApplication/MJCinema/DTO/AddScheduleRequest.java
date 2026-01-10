package com.MovieBookingApplication.MJCinema.DTO;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class AddScheduleRequest {
    @NotNull(message = "Movie Id is required.")
    private Integer movieId;
    @NotNull(message = "Cinema Id is required.")
    private Integer cinemaId;
    @Future(message = "Show date should be in the future.")
    private LocalDate showDate;
    @NotNull(message = "Schedule Slot is required.")
    private ScheduleSlot slot;

    @NotNull(message = "Vip Price required.")
    private Double VipPrice;
    @NotNull(message = "Regular Price required.")
    private Double RegPrice;
    @NotNull(message = "Balcony Price required.")
    private Double BalPrice;

    public void setSlot(ScheduleSlot slot) {
        this.slot = slot;
    }

    public Double getVipPrice() {
        return VipPrice;
    }

    public void setVipPrice(Double vipPrice) {
        VipPrice = vipPrice;
    }

    public Double getRegPrice() {
        return RegPrice;
    }

    public void setRegPrice(Double regPrice) {
        RegPrice = regPrice;
    }

    public Double getBalPrice() {
        return BalPrice;
    }

    public void setBalPrice(Double balPrice) {
        BalPrice = balPrice;
    }

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public Integer getCinemaId() {
        return cinemaId;
    }

    public void setCinemaId(Integer cinemaId) {
        this.cinemaId = cinemaId;
    }

    public LocalDate getShowDate() {
        return showDate;
    }

    public void setShowDate(LocalDate showDate) {
        this.showDate = showDate;
    }

    public ScheduleSlot getSlot() {
        return slot;
    }

    public void setScheduleSlot(ScheduleSlot slot) {
        this.slot = slot;
    }
}
