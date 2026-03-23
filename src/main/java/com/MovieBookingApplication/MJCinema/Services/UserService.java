package com.MovieBookingApplication.MJCinema.Services;

import com.MovieBookingApplication.MJCinema.DTO.*;
import com.MovieBookingApplication.MJCinema.Entity.SeatPrice;
import com.MovieBookingApplication.MJCinema.Entity.TicketStatus;
import com.MovieBookingApplication.MJCinema.Entity.Tickets;
import com.MovieBookingApplication.MJCinema.Entity.Users;
import com.MovieBookingApplication.MJCinema.Repository.SeatPriceRepository;
import com.MovieBookingApplication.MJCinema.Repository.TicketRepository;
import com.MovieBookingApplication.MJCinema.Repository.UserRepository;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class UserService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    SeatPriceRepository seatPriceRepository;
    @Autowired JWTService jwtService;
    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository
                        ,AuthenticationManager authenticationManager){
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
    }

    public boolean registerUser(String username, String password, String confirm){
        boolean isUsernameUsed = userRepository.existsByUsername(username);

        if(isUsernameUsed){
            return false;
        }
        else{
            Users u = new Users();
            u.setUsername(username);
            u.setPassword(passwordEncoder.encode(password));
            u.setRole("USER");
            userRepository.save(u);
            return true;
        }
    }

    public Map<String, Object> loginUser(UserDTO user) {
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        Map<String, Object> output = new HashMap<>();
        if (authentication.isAuthenticated()) {
            Users userLoggedIn = userRepository.findByUsername(user.getUsername()).orElseThrow(
                    () -> new RuntimeException("User not found."));
            String token =  jwtService.generateToken(user.getUsername());
                output.put("userId", userLoggedIn.getUserId());
                output.put("username", userLoggedIn.getUsername());
                output.put("jwtToken", token);
                output.put("role", userLoggedIn.getRole());

                return output;
        }
        return null;
    }
    public List<MovieTicketsDTO> showTickets(String username){
        return ticketRepository.FindTicketsByUserUsername(username);
    }

    public String changePassword(ChangePasswordRequest request, String username){

        Users user  = userRepository.findByUsername(username).
                orElseThrow(() -> new RuntimeException("User not found."));

        if(checkPassword(request.getOldPassword(), username)){
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
        }
        else{
            return "Password incorrect";
        }
        return "Password Changed Successfully";
    }

    public boolean checkPassword(String password, String username){

        Users user  = userRepository.findByUsername(username).
                orElseThrow(() -> new RuntimeException("User not found."));

        return passwordEncoder.matches(password, user.getPassword());
    }

    public GetUserRequest getUser(Integer userId){
        GetUserRequest currUser = userRepository.findByUserId(userId);

        return currUser; //returns the details of the current user
    }

    public List<ShowUsersResponse> showUsers(){
        List<Users> allUsers = userRepository.findAll();
        List<ShowUsersResponse> usersResponses = userRepository.findAllUsers();
        return usersResponses;
    }

    public Integer showTotalUsers(){
        return userRepository.showTotalUsers();
    }
}
