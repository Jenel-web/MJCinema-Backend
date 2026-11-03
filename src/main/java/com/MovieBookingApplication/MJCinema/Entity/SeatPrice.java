package com.MovieBookingApplication.MJCinema.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "seat_price", indexes = {
        //the price of one seat category in one showtime (booking, cancelling, revenue reports)
        @Index(name = "idx_seatprice_schedule_category", columnList = "schedule_id, seat_category")
})
public class SeatPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_price_id")
    private Integer seatPriceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule; // there will be many schedules with the same seat price.

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_category", nullable = false)
    private SeatCategory seatCategory; // will only have 3 values

    @Column(nullable = false)
    private Double price;

    public Integer getSeatPriceId() {
        return seatPriceId;
    }

    public void setSeatPriceId(Integer seatPriceId) {
        this.seatPriceId = seatPriceId;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public SeatCategory getSeatCategory() {
        return seatCategory;
    }

    public void setSeatCategory(SeatCategory seatCategory) {
        this.seatCategory = seatCategory;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
