package com.MovieBookingApplication.MJCinema.Controllers;

import com.MovieBookingApplication.MJCinema.DTO.*;
import com.MovieBookingApplication.MJCinema.Services.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@CrossOrigin(origins = "http://127.0.0.1:5500", allowCredentials = "true", allowedHeaders = "*")//can be used when credentials are included.
@RestController
@RequestMapping("/ticket")
public class TicketsController {

    @Autowired
    private TicketService ticketService;

    @GetMapping("/user")
    public String currentUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not logged in");
        }
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
    public ResponseEntity<Map<String, String>> cancelTicket(@RequestBody CancelTicketRequest request, Authentication auth){
        Map<String, String> errorMessage = new HashMap<>();

        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be logged in to cancel tickets.");
        }
        if (auth == null) {
            System.out.println("ERROR: Authentication object is null!");
            errorMessage.put("message", "Not Authenticated.");
            return ResponseEntity.status(401).body(errorMessage);
        }
        // 2. Pass the username directly to the service
       Map<String, String> result  = ticketService.cancelTicket(request.getTicketCode(), auth.getName() ); //passes the name from auth

        return ResponseEntity.ok(result);
    }

    @GetMapping("/mytickets")
    public ResponseEntity<List<MovieTicketsDTO>> showMyTickets(@RequestParam Integer userId){
        List<MovieTicketsDTO> myTickets = ticketService.showMyTickets(userId);

        return ResponseEntity.ok(myTickets);
    }

    @GetMapping("/totalSales")
    public ResponseEntity<Double> showTotalSales(){
        Double totalSales = ticketService.showTotalSales();

        return ResponseEntity.ok(totalSales);
    }

    @GetMapping("/showBookings")
    public ResponseEntity<List<ShowBookingsResponse>> showBookings(){
        List<ShowBookingsResponse> allBookingsActive = ticketService.showBookings();

        return ResponseEntity.ok(allBookingsActive);
    }

    @GetMapping("/showTotalBookings")
    public ResponseEntity<Integer> showTotalBookings(){
        Integer total = ticketService.showTotalBookings();

        return ResponseEntity.ok(total);
    }
}
