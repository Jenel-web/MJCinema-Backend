package com.MovieBookingApplication.MJCinema.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"schedule_id", "seat_id"})
        }   // makes sure that there are no double booking.
)

public class Tickets {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Integer ticketId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user; // only one user per ticket but there can be many tickets for the same user

    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule; //many tickets can be having the same schedule id but only one schedule per ticket

    @ManyToOne
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
