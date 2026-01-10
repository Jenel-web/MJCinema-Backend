package com.MovieBookingApplication.MJCinema.DTO;

import jakarta.validation.constraints.NotNull;

public class ShowMovieRequest {
    @NotNull(message = "Movie Id is required.")
    private Integer movieId;

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }
}
