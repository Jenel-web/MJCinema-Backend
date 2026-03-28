package com.MovieBookingApplication.MJCinema.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MovieSearchDTO {
    private String posterPath;
    private String title;
    private Double vote_average;
    private String overview;
    private String release_date;
    private Long id;

    public MovieSearchDTO(String posterPath, String title, Double vote_average, String overview, String release_date, Long id) {
        this.posterPath = posterPath;
        this.title = title;
        this.vote_average = vote_average;
        this.overview = overview;
        this.release_date = release_date;
        this.id = id;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Double getVote_average() {
        return vote_average;
    }

    public void setVote_average(Double vote_average) {
        this.vote_average = vote_average;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
