package com.MovieBookingApplication.MJCinema.Controllers;

import com.MovieBookingApplication.MJCinema.DTO.*;
import com.MovieBookingApplication.MJCinema.Entity.Schedule;
import com.MovieBookingApplication.MJCinema.Entity.SeatPrice;
import com.MovieBookingApplication.MJCinema.Services.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping ("/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping("/{id}")
    public ResponseEntity<Schedule> findSchedule(@PathVariable ("id")Integer scheduleId){
        Optional<Schedule> scheduleOpt = scheduleService.findSchedule(scheduleId);

        // 2. Check if the "box" has something inside
        if (scheduleOpt.isPresent()) {
            return ResponseEntity.ok(scheduleOpt.get()); // Extract the Schedule and return 200 OK
        } else {
            return ResponseEntity.notFound().build(); // Return 404 if not found
        }
    }

    @GetMapping("/now-showing")
    public List<MovieDetailsDTO> nowShowing() {
        return scheduleService.nowShowing();
    }

    @GetMapping("/seats")
    public ResponseEntity<Map<String, List<ShowAvailableSeatsResponse>>> showAvailableSeats(@RequestParam("id") Integer scheduleId) {
        Map<String, List<ShowAvailableSeatsResponse>> map = scheduleService.showAvailableSeats(scheduleId);
        return ResponseEntity.ok(map);
    }

    @GetMapping("/coming-soon")
    public List<ComingSoonResponse> comingSoon() {
        return scheduleService.comingSoon();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<String> addSchedule(@Valid @RequestBody AddScheduleRequest request) {
        String response = scheduleService.addSchedule(request);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/cancel/{id}") //working already.
    public ResponseEntity<String> cancelSchedule(@Valid @PathVariable("id") Integer scheduleId) {
        String result = scheduleService.removeSchedule(scheduleId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/movieSchedules/{id}")
    public ResponseEntity<List<Schedule>> findSchedules(@PathVariable("id") Integer movieId) {
        List<Schedule> nowShowingSchedules = scheduleService.findSchedules(movieId);

        return ResponseEntity.ok(nowShowingSchedules);
    }

    @GetMapping("/schedPrice/{id}")
    public ResponseEntity<List<SeatPrice>> findSchedPrices(@PathVariable("id") Integer scheduleId) {
        List<SeatPrice> seatPrices =  scheduleService.findSeatPricesBySchedule(scheduleId);

        return ResponseEntity.ok(seatPrices);
    }

}
