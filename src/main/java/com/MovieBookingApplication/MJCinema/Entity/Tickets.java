package com.MovieBookingApplication.MJCinema.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets", indexes = {
        //"tickets of this showtime that are not cancelled": the booking check, the seat map and the seat stats.
        //schedule_id goes first because every one of those queries filters by it; ticket_status refines it.
        @Index(name = "idx_tickets_schedule_status", columnList = "schedule_id, ticket_status"),
        //the newest bookings first (the admin bookings list); without it MySQL sorts every ticket to find the latest 20.
        @Index(name = "idx_tickets_booked_time", columnList = "booked_time")
})
public class Tickets {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Integer ticketId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",referencedColumnName = "user_id", nullable = false)
    private Users user; // only one user per ticket but there can be many tickets for the same user

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule; //many tickets can be having the same schedule id but only one schedule per ticket

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat; // one ticket per seat but there can be many tickets with the same seat but diff schedule.


    @Column(name = "ticket_code", nullable = false, unique = true)
    private String ticketCode; //must be unique

    @Column(name = "booked_time", nullable = false)
    private LocalDateTime bookedTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_status", nullable = true)
    public TicketStatus ticketStatus;

    public TicketStatus getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(TicketStatus ticketStatus) {
        this.ticketStatus = ticketStatus;
    }

    public Integer getTicketId() {
        return ticketId;
    }

    public void setTicketId(Integer ticketId) {
        this.ticketId = ticketId;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public LocalDateTime getBookedTime() {
        return bookedTime;
    }

    public void setBookedTime(LocalDateTime bookedTime) {
        this.bookedTime = bookedTime;
    }
}
