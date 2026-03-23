package com.MovieBookingApplication.MJCinema.DTO;

public class MovieLeaderboardResponse {
    private String title;
    private Double revenue;

    public MovieLeaderboardResponse(String title, Double revenue) {
        this.title = title;
        this.revenue = revenue;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Double getRevenue() {
        return revenue;
    }

    public void setRevenue(Double revenue) {
        this.revenue = revenue;
    }
}
