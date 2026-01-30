package com.MovieBookingApplication.MJCinema.Controllers;

import com.MovieBookingApplication.MJCinema.DTO.*;
import com.MovieBookingApplication.MJCinema.Services.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping ("/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping("/now-showing")
    public List<MovieDetailsDTO> nowShowing(){
        return scheduleService.nowShowing();
    }

    @GetMapping("/seats")
    public ResponseEntity <Map<String, List<ShowAvailableSeatsResponse>>> showAvailableSeats(@RequestParam ("id") Integer scheduleId){
        Map<String, List<ShowAvailableSeatsResponse>> map = scheduleService.showAvailableSeats(scheduleId);
         return ResponseEntity.ok(map);
    }

    @GetMapping("/coming-soon")
    public List<ComingSoonResponse> comingSoon(){
        return scheduleService.comingSoon();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<String> addSchedule(@Valid @RequestBody AddScheduleRequest request){
        String response = scheduleService.addSchedule(request);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/cancel/{id}") //working already.
    public ResponseEntity<String> cancelSchedule(@Valid @PathVariable ("id") Integer scheduleId){
       String result = scheduleService.removeSchedule(scheduleId);
        return ResponseEntity.ok(result);
    }


}
