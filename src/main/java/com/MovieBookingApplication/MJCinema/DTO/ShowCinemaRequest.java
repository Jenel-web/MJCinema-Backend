package com.MovieBookingApplication.MJCinema.DTO;

public class ShowCinemaRequest {
    private String location;
    private Integer cinemaId;
    private String cinemaName;

    public ShowCinemaRequest(String location, Integer cinemaId, String cinemaName) {
        this.location = location;
        this.cinemaId = cinemaId;
        this.cinemaName = cinemaName;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public Integer getCinemaId() {
        return cinemaId;
    }

    public void setCinemaId(Integer cinemaId) {
        this.cinemaId = cinemaId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
