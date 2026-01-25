package com.MovieBookingApplication.MJCinema.Controllers;

import com.MovieBookingApplication.MJCinema.DTO.ChangePasswordRequest;
import com.MovieBookingApplication.MJCinema.DTO.MovieTicketsDTO;
import com.MovieBookingApplication.MJCinema.DTO.RegisterRequest;
import com.MovieBookingApplication.MJCinema.DTO.UserDTO;
import com.MovieBookingApplication.MJCinema.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/healthy")
    public ResponseEntity<String> healthy(){
        return ResponseEntity.ok("healthy");
    }

    @GetMapping
    public String currentUser(Authentication auth){
        String username = auth.getName();

        return username;
    }
    @PostMapping("/register")
    public ResponseEntity <Map<String, String>> registerUser(@RequestBody RegisterRequest user){
        Map <String, String> response = new HashMap<>();

        //checks if the two passwords match each other
        if(!user.getPassword().equals(user.getConfirm())){
            response.put("error", "Passwords Mismatch");
            response.put("message", "Passwords do not match. Please try again");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        boolean isRegistered = userService.registerUser(user.getUsername(),
                user.getPassword(), user.getConfirm());
        //checks if the username is not used yet.
        if(isRegistered) {
            response.put("username", user.getUsername());
            response.put("message", "Welcome to MJCinema, " + user.getUsername()+ "!");
            return ResponseEntity.ok(response);
        }
        else {
            // Handle case where username might already be taken
            response.put("error", "Duplicate User");
            response.put("message", "Username is already in use.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response); // 409 Conflict
        }
    }

    @PostMapping("/login")
    public ResponseEntity <Map<String, String>> loginUSer(@RequestBody UserDTO user){
        boolean isLoggedIn = userService.loginUser(user.getUsername(), user.getPassword());
        Map<String, String> login = new HashMap<>();
        if(isLoggedIn){
            login.put("user", "User logged in: " + user.getUsername());
            login.put("message", "Welcome back" + user.getUsername() +
                    "!");
            return ResponseEntity.ok(login);
        }
        else{
            login.put("message", "Invalid username or password");
            return ResponseEntity.status(401).body(login);
        }
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
