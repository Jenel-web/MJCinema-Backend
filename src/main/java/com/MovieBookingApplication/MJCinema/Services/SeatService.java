package com.MovieBookingApplication.MJCinema.Services;

import com.MovieBookingApplication.MJCinema.Entity.Seat;
import com.MovieBookingApplication.MJCinema.Entity.Tickets;
import com.MovieBookingApplication.MJCinema.Repository.SeatRespository;
import com.MovieBookingApplication.MJCinema.Repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SeatService {

    @Autowired
    private SeatRespository seatRespository;

    @Autowired
    private TicketRepository ticketRepository;

    public List<String> findOccupiedSeats(Integer scheduleId){

        List<Tickets> bookedTickets = ticketRepository.findByScheduleScheduleId(scheduleId);

        List<String> occupiedSeats = new ArrayList<>();

        for(Tickets t: bookedTickets){
            occupiedSeats.add(t.getSeat().getSeatNumber());
        }

        return occupiedSeats;
    }

}
