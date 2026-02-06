    package com.MovieBookingApplication.MJCinema.Controllers;

    import com.MovieBookingApplication.MJCinema.Entity.Seat;
    import com.MovieBookingApplication.MJCinema.Services.SeatService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;

    @CrossOrigin(origins = "*")
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
