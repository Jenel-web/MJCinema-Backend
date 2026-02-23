package com.MovieBookingApplication.MJCinema.Services;

import com.MovieBookingApplication.MJCinema.DTO.MovieTicketsDTO;
import com.MovieBookingApplication.MJCinema.DTO.TicketDTO;
import com.MovieBookingApplication.MJCinema.Entity.*;
import com.MovieBookingApplication.MJCinema.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TicketService {


    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private SeatPriceRepository seatPriceRepository;

    @Autowired
    private SeatRespository seatRespository;

    @Transactional //cancels if not perfectly executed
    public TicketDTO bookTicket(Integer userId, Integer scheduleId, List<String> selectedSeat){

       //finding an entry on the db automatically creates an element that is of the same type
        // where you can fetch the data in the entry by calling the declared name.


        // 1. validate user
        Users user = userRepository.findById(userId).orElseThrow(()-> new UsernameNotFoundException("User not found!"));
        // 2. validate schedule
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(() -> new RuntimeException("Schedule not found"));
        // 3. validate seat
        Cinema cinema = schedule.getCinema();
        List<Tickets> ticketsBooked = new ArrayList<>();
        for(String s: selectedSeat) {
            Tickets ticket = new Tickets();
            Seat seatNumber = seatRespository.findBySeatNumberAndCinemaCinemaId(
                    s, cinema.getCinemaId()).orElseThrow(() -> new RuntimeException("Seat not found."));
            // this creates a seat object that is found using the function.
            // 4. check availability
            boolean isTaken = ticketRepository.existsByScheduleScheduleIdAndSeatSeatNumber(scheduleId, s);

            if (isTaken) {
                throw new RuntimeException("Seat is already taken.");
            }
            // 5. calculate price and deduct
            SeatPrice price = seatPriceRepository.findBySeatCategoryAndScheduleScheduleId(seatNumber.getSeatCategory(),
                    scheduleId).orElseThrow(() -> new RuntimeException("Price not found."));

            Double ticketPrice = price.getPrice();
            Double currentBalance = user.getBalance();
            if (currentBalance < ticketPrice) {
                throw new RuntimeException("Insufficient balance.");
                //throw is the word for errors. not return!!!!
            }
            user.setBalance(currentBalance - ticketPrice);
            // 6. save ticket
            ticket.setUser(user);
            ticket.setSchedule(schedule);
            ticket.setSeat(seatNumber);
            ticket.setTicketCode("MJ-" + UUID.randomUUID().toString().substring(0, 6));
            ticket.setBookedTime(LocalDateTime.now());
            ticketsBooked.add(ticket);
        }
        ticketRepository.saveAll(ticketsBooked);
        return DTOWrapper(ticketsBooked.get(0)); //returns the first ticket in the frontend.
    }



    public TicketDTO DTOWrapper(Tickets ticket){
        TicketDTO ticketDTO = new TicketDTO();

        ticketDTO.setName(ticket.getUser().getUsername());
        ticketDTO.setTitle(ticket.getSchedule().getMovie().getTitle());
        ticketDTO.setSeat(ticket.getSeat().getSeatNumber());
        ticketDTO.setCinema("MJCinema");
        SeatPrice ticketPrice = seatPriceRepository.findBySeatCategoryAndScheduleScheduleId(ticket.getSeat().getSeatCategory(),
                ticket.getSchedule().getScheduleId()).orElseThrow(() -> new RuntimeException("Price not found."));
        // if its optional, always remember to throw a runtime exception since you are not sure if there is a value or not.
        ticketDTO.setPrice(ticketPrice.getPrice());
        ticketDTO.setTicketCode(ticket.getTicketCode());
        ticketDTO.setSeatCategory(ticket.getSeat().getSeatCategory());
        //make the enum a string so it can be displayed using toString()
        // if you declared it as a string.
        ticketDTO.setStatus("SUCCESS");
        ticketDTO.setShowDate(ticket.getSchedule().getShowDate());
        ticketDTO.setShowTime(ticket.getSchedule().getStartTime());

        return ticketDTO;

    }

    public List<MovieTicketsDTO> showMovieTickets(Integer movieId){
        return ticketRepository.FindTicketsByMovie(movieId);
    }

    @Transactional
    public String cancelTicket(String ticketCode, String username){

        //find the user
        Users user = userRepository.findByUsername(username).orElseThrow(()-> new UsernameNotFoundException("User not found!"));
        //find if the ticket owner of the ticket is the same and cancel if yes.
        Tickets ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new RuntimeException("Ticket not found."));


        if(!ticket.getUser().getUsername().equals(username)){
            throw new RuntimeException("Ticket Cancellation Failed.");
        }

        //delete ticket
       ticket.setTicketStatus(TicketStatus.CANCELLED);
        ticketRepository.save(ticket);
        //add the ticket price to balance
        Double balance = user.getBalance();
        SeatPrice seatPrice = seatPriceRepository.findBySeatCategoryAndScheduleScheduleId(ticket.getSeat().getSeatCategory(),
                ticket.getSchedule().getScheduleId()).orElseThrow(() -> new RuntimeException("Price not found."));
        Double ticketPrice = seatPrice.getPrice();
        user.setBalance(balance + ticketPrice);
        userRepository.save(user);
        return "Ticket Deleted Successfully!";
    }
    public List<MovieTicketsDTO> showMyTickets(Integer userId){
        List<MovieTicketsDTO> showMytickets = ticketRepository.findByUserUserId(userId);

        return showMytickets;

    }

}
