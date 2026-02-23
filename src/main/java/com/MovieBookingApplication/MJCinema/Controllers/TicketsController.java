package com.MovieBookingApplication.MJCinema.Controllers;

import com.MovieBookingApplication.MJCinema.DTO.*;
import com.MovieBookingApplication.MJCinema.Services.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@CrossOrigin(origins = "http://127.0.0.1:5500", allowCredentials = "true")//can be used when credentials are included.
@RestController
@RequestMapping("/ticket")
public class TicketsController {

    @Autowired
    private TicketService ticketService;

    @GetMapping("/user")
    public String currentUser(Authentication auth){
        String name = auth.getName();

        return name;
    }
   // @PreAuthorize("hasRole('USER')")
    @PostMapping("/book")
    //you can only have one requestbody when making a post http method so
    // you need to make a request dto and encapsulate the variables.
   public ResponseEntity<TicketDTO> bookTicket (@RequestBody BookingRequest request){
       TicketDTO ticket = ticketService.bookTicket(request.getUserId(), request.getScheduleId(), request.getSelectedSeat());

       return ResponseEntity.ok(ticket);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/show")
    public ResponseEntity<List<MovieTicketsDTO>> showMovieTickets(@RequestParam ("id") Integer movieId){
        List<MovieTicketsDTO> tickets =  ticketService.showMovieTickets(movieId);

        return ResponseEntity.ok(tickets);
    }

    @PatchMapping("/cancel")
    public ResponseEntity<String> cancelTicket(@RequestBody CancelTicketRequest request, Authentication auth){
        String message = ticketService.cancelTicket(request.getTicketCode(), currentUser(auth));

        return ResponseEntity.ok(message);
    }

    @GetMapping("/mytickets")
    public ResponseEntity<List<MovieTicketsDTO>> showMyTickets(@RequestParam Integer userId){
        List<MovieTicketsDTO> myTickets = ticketService.showMyTickets(userId);

        return ResponseEntity.ok(myTickets);
    }

}
