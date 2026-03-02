package com.MovieBookingApplication.MJCinema.DTO;

public class GetUserRequest {
    private String username;
    private Double balance;

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
}
