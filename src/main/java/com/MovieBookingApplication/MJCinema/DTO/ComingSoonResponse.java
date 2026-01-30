package com.MovieBookingApplication.MJCinema.DTO;

import java.time.LocalDate;

public class ComingSoonResponse {
    private String poster;
    private String title;
    private Double rating;
    private String overview;
    private LocalDate releaseDate;
    private LocalDate showDate;

    public ComingSoonResponse(String poster, String title, Double rating, String overview, LocalDate releaseDate, LocalDate showDate) {
        this.poster = poster;
        this.title = title;
        this.rating = rating;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.showDate = showDate;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public LocalDate getShowDate() {
        return showDate;
    }

    public void setShowDate(LocalDate showDate) {
        this.showDate = showDate;
    }
}
