package com.MovieBookingApplication.MJCinema.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MovieSearchDTO {
    private String posterPath;
    private String title;
    @JsonProperty("poster_path")
    @JsonProperty("release_date")
    private String release_date;
    @JsonProperty("vote_average")
    private Double vote_average;
}
