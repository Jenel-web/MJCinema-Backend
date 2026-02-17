package com.MovieBookingApplication.MJCinema.DTO;

public class ShowCinemaRequest {
    private String location;
    private Integer cinemaId;

    public ShowCinemaRequest(String location, Integer cinemaId) {
        this.location = location;
        this.cinemaId = cinemaId;
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
