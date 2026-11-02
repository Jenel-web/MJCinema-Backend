package com.MovieBookingApplication.MJCinema.Services;

import com.MovieBookingApplication.MJCinema.Repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeatService {

    @Autowired
    private TicketRepository ticketRepository;

    public List<String> findOccupiedSeats(Integer scheduleId){
        //only the seat numbers are needed, so there is no need to load the tickets.
        return ticketRepository.FindSeatSeatNumberByScheduleId(scheduleId);
    }

}
