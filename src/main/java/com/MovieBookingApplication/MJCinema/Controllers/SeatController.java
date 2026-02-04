package com.MovieBookingApplication.MJCinema.Controllers;

import com.MovieBookingApplication.MJCinema.Entity.Seat;
import com.MovieBookingApplication.MJCinema.Services.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/seat")
public class SeatController {

    @Autowired
    private SeatService seatService;

    @GetMapping("/occupied/{id}")
    public ResponseEntity<List<String>> findOccupiedSeats(@PathVariable ("id") Integer scheduleId){
        List<String> occupiedSeats = seatService.findOccupiedSeats(scheduleId);

        return ResponseEntity.ok(occupiedSeats);
    }

}
