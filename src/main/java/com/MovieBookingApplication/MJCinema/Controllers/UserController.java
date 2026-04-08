package com.MovieBookingApplication.MJCinema.Controllers;

import com.MovieBookingApplication.MJCinema.DTO.*;
import com.MovieBookingApplication.MJCinema.Entity.Users;
import com.MovieBookingApplication.MJCinema.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "https://mjcinemaapp.vercel.app/")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;


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
            response.put("userId", user.getPassword());
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
    public ResponseEntity <Map<String, Object>> loginUSer(@RequestBody UserDTO user){
      Map<String, Object> output = userService.loginUser(user);
        Map<String, Object> login = new HashMap<>();

        if(output != null){
            return ResponseEntity.ok(output);
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

    @GetMapping("/getUser")
    public ResponseEntity<Map<String, Object>> getUser(@RequestParam Integer userId){
        GetUserRequest user = userService.getUser(userId);
        Map<String, Object> userInfo = new HashMap<>(); //returns an object so
        //it can map to any reference type variable/.

        userInfo.put("username", user.getUsername());
        userInfo.put("balance", user.getBalance());
        return ResponseEntity.ok(userInfo);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/showUsers")
    public ResponseEntity<List<ShowUsersResponse>> showUsers(){
        List<ShowUsersResponse> users = userService.showUsers();

        return ResponseEntity.ok(users);
    }

    @GetMapping("/showTotalUsers")
    public ResponseEntity<Integer> showTotalUsers(){
        Integer total = userService.showTotalUsers();

        return ResponseEntity.ok(total);
    }
}
