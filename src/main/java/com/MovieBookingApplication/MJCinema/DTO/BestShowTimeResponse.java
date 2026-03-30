package com.MovieBookingApplication.MJCinema.DTO;

public class BestShowTimeResponse {
    private ScheduleSlot slot;
    private Double averageRevenue;

    public BestShowTimeResponse(ScheduleSlot slot, Double averageRevenue) {
        this.slot = slot;
        this.averageRevenue = averageRevenue;
    }

    public ScheduleSlot getSlot() {
        return slot;
    }

    public void setSlot(ScheduleSlot slot) {
        this.slot = slot;
    }

    public Double getAverageRevenue() {
        return averageRevenue;
    }

    public void setAverageRevenue(Double averageRevenue) {
        this.averageRevenue = averageRevenue;
    }
}
