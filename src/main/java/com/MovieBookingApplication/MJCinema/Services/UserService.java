package com.MovieBookingApplication.MJCinema.Services;

import com.MovieBookingApplication.MJCinema.DTO.ChangePasswordRequest;
import com.MovieBookingApplication.MJCinema.DTO.MovieTicketsDTO;
import com.MovieBookingApplication.MJCinema.Entity.Users;
import com.MovieBookingApplication.MJCinema.Repository.TicketRepository;
import com.MovieBookingApplication.MJCinema.Repository.UserRepository;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;



@Service
public class UserService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private TicketRepository ticketRepository;

    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository){
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
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

    public boolean loginUser(String username, String password){
        //find in repo
        return userRepository.findByUsername(username)
                .map( user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(false);

        //return true or false
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

}
