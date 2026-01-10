package com.MovieBookingApplication.MJCinema.DTO;

import java.time.LocalTime;

public enum ScheduleSlot {
    MORNING("09:00:00" ,"11:00:00"),
    MIDDAY("11:30:00" , "01:30:00"),
    AFTERNOON("14:00:00" , "16:00:00"),
    EVENING("16:30:00" , "18:30:00"),
    NIGHT("19:00:00" , "21:00:00");

    private String startTime;
    private String endTime;

     ScheduleSlot(String startTime, String endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public LocalTime getStartTime(){
        return LocalTime.parse(startTime);
    }

    public LocalTime getEndTime(){
        return LocalTime.parse(endTime); //remember to parse since its string.
    }
}