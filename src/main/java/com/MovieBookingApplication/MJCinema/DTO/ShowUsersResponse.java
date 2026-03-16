package com.MovieBookingApplication.MJCinema.DTO;

public class ShowUsersResponse {
    private String username;
    private Double balance;
    private Long ticketsBooked;
    private Double totalSpent;

    public ShowUsersResponse(String username, Double balance, Long ticketsBooked, Double totalSpent) {
        this.username = username;
        this.balance = balance;
        this.ticketsBooked = ticketsBooked;
        this.totalSpent = (totalSpent != null) ? totalSpent : 0.0;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Long getTicketsBooked() {
        return ticketsBooked;
    }

    public void setTicketsBooked(Long ticketsBooked) {
        this.ticketsBooked = ticketsBooked;
    }

    public Double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(Double totalSpent) {
        this.totalSpent = totalSpent;
    }
}
