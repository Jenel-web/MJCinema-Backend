package com.MovieBookingApplication.MJCinema.DTO;

import jakarta.validation.constraints.NotNull;

public class CancelTicketRequest {
    @NotNull(message = "Ticket Code is required.")
    private String ticketCode;

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }
}
