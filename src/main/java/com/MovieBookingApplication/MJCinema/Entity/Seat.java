package com.MovieBookingApplication.MJCinema.Entity;

import jakarta.persistence.*;

@Entity
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Integer seatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cinema_id", nullable = false)
    private Cinema cinema; // should be the entity to make a foreign key
    // it connects to the table and finds the fk.

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_category", nullable = false)
    private SeatCategory seatCategory;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    public Integer getSeatId() {
        return seatId;
    }

    public void setSeatId(Integer seatId) {
        this.seatId = seatId;
    }

    public Cinema getCinema() {
        return cinema;
    }

    public void setCinema(Cinema cinema) {
        this.cinema = cinema;
    }

    public SeatCategory getSeatCategory() {
        return seatCategory;
    }

    public void setSeatCategory(SeatCategory seatCategory) {
        this.seatCategory = seatCategory;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }
}
