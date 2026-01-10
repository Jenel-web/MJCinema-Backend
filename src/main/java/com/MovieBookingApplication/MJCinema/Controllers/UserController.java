package com.MovieBookingApplication.MJCinema.Controllers;

import com.MovieBookingApplication.MJCinema.DTO.ChangePasswordRequest;
import com.MovieBookingApplication.MJCinema.DTO.MovieTicketsDTO;
import com.MovieBookingApplication.MJCinema.DTO.UserDTO;
import com.MovieBookingApplication.MJCinema.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/healthy")
    public ResponseEntity<String> healthy(){
        return ResponseEntity.ok("healthy");
    }

    @GetMapping("/user")
    public String currentUser(Authentication auth){
        String username = auth.getName();

        return username;
    }
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDTO user){
        userService.registerUser(user.getUsername(), user.getPassword());
        return ResponseEntity.ok("User registered successfully.");

    }

    @GetMapping("/tickets")
    public ResponseEntity<List<MovieTicketsDTO>> showTickets(Authentication auth){
        List<MovieTicketsDTO> userTickets =  userService.showTickets(currentUser(auth));

        return ResponseEntity.ok(userTickets);
    }

    @PatchMapping("/change/password")
    public ResponseEntity<String> changePassword (@RequestBody ChangePasswordRequest request,
                                                  Authentication auth){
        String result = userService.changePassword(request, currentUser(auth));
        return ResponseEntity.ok(result);
    }
}
