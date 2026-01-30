package com.MovieBookingApplication.MJCinema.Entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movie_id")
    private Integer movieId;

    // will be taken from tmdb and stored in the db but only some parts are shown in the dto.
    @Column(name = "tmdb_id",nullable = false, unique = true)
    private Long tmdbId;
    @Column(nullable = false)
    private String poster;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String duration;
    @Column(nullable = false)
    private Double rating;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String overview; // made as text in db because some overiew are longer than 255 characters.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovieStatus status;
    @Column(name = "release_date",nullable = false) //should be snake case in db and camel case in java.
    private LocalDate releaseDate;

    public void setStatus(MovieStatus status) {
        this.status = status;
    }

    public MovieStatus getStatus() {
        return status;
    }

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public Long getTmdbId() {
        return tmdbId;
    }

    public void setTmdbId(Long tmdbId) {
        this.tmdbId = tmdbId;
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

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
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
}
