package com.MovieBookingApplication.MJCinema.DTO;

import com.MovieBookingApplication.MJCinema.Entity.TicketStatus;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

public class ShowBookingsResponse {
    private String ticketCode;
    private String username;
    private String seatNumber;
    private LocalDate showDate;
    private String cinemaName;
    private LocalTime startTime;
    private TicketStatus ticketStatus;
    public ShowBookingsResponse(String ticketCode,
                                String username,
                                String seatNumber,
                                LocalDate showDate,
                                String cinemaName,
                                LocalTime startTime,
                                TicketStatus ticketStatus) {
        this.ticketCode = ticketCode;
        this.username = username;
        this.seatNumber = seatNumber;
        this.showDate = showDate;
        this.cinemaName = cinemaName;
        this.startTime = startTime;
        this.ticketStatus = ticketStatus;
    }


    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public LocalDate getShowDate() {
        return showDate;
    }

    public void setShowDate(LocalDate showDate) {
        this.showDate = showDate;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public TicketStatus getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(TicketStatus ticketStatus) {
        this.ticketStatus = ticketStatus;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }
}
