package com.MovieBookingApplication.MJCinema.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)//ignores properties that are not found in dto
public class TmdbMovieDTO {
    //automatically get the data per movie in each movie.
    private Long id;
    private String title;
    private String overview;

    @JsonProperty("poster_path")
    private String posterPath;
    @JsonProperty("release_date")
    private String releaseDate;
    @JsonProperty("vote_average")
    private Double voteAverage;

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setRelease_date(String release_date) {
        this.releaseDate = release_date;
    }

    public Double getVoteAverage() {
        return voteAverage;
    }

    public void setVote_average(Double vote_average) {
        this.voteAverage = vote_average;
    }
}
