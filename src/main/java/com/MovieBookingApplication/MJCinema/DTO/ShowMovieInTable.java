package com.MovieBookingApplication.MJCinema.DTO;

import com.MovieBookingApplication.MJCinema.Entity.MovieStatus;

import java.time.LocalDate;

public class ShowMovieInTable {
        private String title;
        private Double rating;
        private LocalDate releaseDate;
        private MovieStatus status;
        private Double revenue;

    public ShowMovieInTable(String title,
                            Double rating,
                            LocalDate releaseDate,
                            MovieStatus status,
                            Double revenue) {
        this.title = title;
        this.rating = rating;
        this.releaseDate = releaseDate;
        this.status = status;
        this.revenue = revenue;
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

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public MovieStatus getStatus() {
        return status;
    }

    public void setStatus(MovieStatus status) {
        this.status = status;
    }

    public Double getRevenue() {
        return revenue;
    }

    public void setRevenue(Double revenue) {
        this.revenue = revenue;
    }
}
