package com.MovieBookingApplication.MJCinema.Entity;

import com.MovieBookingApplication.MJCinema.DTO.ScheduleSlot;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Integer scheduleId;

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie; // many schedules for one movie but only one movie per schedule

    @ManyToOne
    @JoinColumn(name ="cinema_id",nullable = false)
    private Cinema cinema; // there can be many schedules on one cinema.
    @Column(name = "show_date", nullable = false)
    private LocalDate showDate;
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "slot",nullable = false)
    private ScheduleSlot slot;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = true)
    private ScheduleStatus status;

    public ScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduleStatus status) {
        this.status = status;
    }

    public ScheduleSlot getSlot() {
        return slot;
    }

    public void setSlot(ScheduleSlot slot) {
        this.slot = slot;
    }

    public Integer getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Integer scheduleId) {
        this.scheduleId = scheduleId;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public Cinema getCinema() {
        return cinema;
    }

    public void setCinema(Cinema cinema) {
        this.cinema = cinema;
    }

    public LocalDate getShowDate() {
        return showDate;
    }

    public void setShowDate(LocalDate showDate) {
        this.showDate = showDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
}
