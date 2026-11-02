package com.MovieBookingApplication.MJCinema.Services;

import com.MovieBookingApplication.MJCinema.DTO.MovieTicketsDTO;
import com.MovieBookingApplication.MJCinema.DTO.ShowBookingsResponse;
import com.MovieBookingApplication.MJCinema.DTO.TicketDTO;
import com.MovieBookingApplication.MJCinema.Entity.*;
import com.MovieBookingApplication.MJCinema.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

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

        if(selectedSeat == null || selectedSeat.isEmpty()){
            throw new RuntimeException("No seats selected.");
        }
        //a seat can only be picked once per booking
        List<String> seatNumbers = new ArrayList<>(new LinkedHashSet<>(selectedSeat));

        // 0. lock the schedule first and then the user (always in this order) so that two bookings of the
        // same schedule, or two bookings of the same user, cannot run at the same time
        scheduleRepository.findByIdForUpdate(scheduleId).orElseThrow(() -> new RuntimeException("Schedule not found"));
        // 1. validate user
        Users user = userRepository.findByIdForUpdate(userId).orElseThrow(()-> new UsernameNotFoundException("User not found!"));
        // 2. validate schedule (the schedule and its cinema come in one query)
        Schedule schedule = scheduleRepository.findByIdWithDetails(scheduleId).orElseThrow(() -> new RuntimeException("Schedule not found"));
        if(schedule.getStatus() != ScheduleStatus.ACTIVE){
            throw new RuntimeException("Schedule is not available for booking.");
        }
        Cinema cinema = schedule.getCinema();

        // 3. validate seats: every lookup is done once for all the seats instead of once per seat
        Map<String, Seat> seats = new HashMap<>();
        for(Seat seat: seatRespository.findByCinemaCinemaIdAndSeatNumberIn(cinema.getCinemaId(), seatNumbers)){
            seats.put(seat.getSeatNumber(), seat);
        }
        // 4. check availability
        Set<String> taken = new HashSet<>(ticketRepository.findTakenSeatNumbers(scheduleId, seatNumbers));
        // 5. prices of the schedule per seat category
        Map<SeatCategory, Double> prices = new HashMap<>();
        for(SeatPrice sp: seatPriceRepository.findByScheduleScheduleId(scheduleId)){
            prices.put(sp.getSeatCategory(), sp.getPrice());
        }

        List<Tickets> ticketsBooked = new ArrayList<>();
        Map<Tickets, Double> ticketPrices = new HashMap<>();
        double total = 0;
        for(String s: seatNumbers) {
            Seat seat = seats.get(s);
            if(seat == null){
                throw new RuntimeException("Seat not found.");
            }
            if (taken.contains(s)) {
                throw new RuntimeException("Seat is already taken.");
            }
            Double ticketPrice = prices.get(seat.getSeatCategory());
            if(ticketPrice == null){
                throw new RuntimeException("Price not found.");
            }
            total += ticketPrice;

            Tickets ticket = new Tickets();
            ticket.setUser(user);
            ticket.setSchedule(schedule);
            ticket.setSeat(seat);
            ticket.setTicketCode("MJ-" + UUID.randomUUID().toString().substring(0, 6));
            ticket.setTicketStatus(TicketStatus.ACTIVE);
            ticket.setBookedTime(LocalDateTime.now());
            ticketsBooked.add(ticket);
            ticketPrices.put(ticket, ticketPrice);
        }

        // 6. deduct the total once
        Double currentBalance = user.getBalance();
        if (currentBalance < total) {
            throw new RuntimeException("Insufficient balance.");
            //throw is the word for errors. not return!!!!
        }
        user.setBalance(currentBalance - total);
        ticketRepository.saveAll(ticketsBooked);

        Tickets first = ticketsBooked.get(0);
        return toTicketDTO(first, ticketPrices.get(first)); //returns the first ticket in the frontend.
    }

    //builds the response from data that is already loaded, so it does not query again.
    private TicketDTO toTicketDTO(Tickets ticket, Double price){
        TicketDTO ticketDTO = new TicketDTO();

        ticketDTO.setName(ticket.getUser().getUsername());
        ticketDTO.setTitle(ticket.getSchedule().getMovie().getTitle());
        ticketDTO.setSeat(ticket.getSeat().getSeatNumber());
        ticketDTO.setCinema("MJCinema");
        ticketDTO.setPrice(price);
        ticketDTO.setTicketCode(ticket.getTicketCode());
        ticketDTO.setSeatCategory(ticket.getSeat().getSeatCategory());
        ticketDTO.setStatus("SUCCESS");
        ticketDTO.setShowDate(ticket.getSchedule().getShowDate());
        ticketDTO.setShowTime(ticket.getSchedule().getStartTime());

        return ticketDTO;
    }

    public List<MovieTicketsDTO> showMovieTickets(Integer movieId){
        return ticketRepository.FindTicketsByMovie(movieId);
    }

    @Transactional
    public Map<String, String> cancelTicket(String ticketCode, String username){
        Map<String, String> cancelTicks = new HashMap<>();
        //find the user and lock the row, so two cancellations of the same user cannot refund twice
        Users user = userRepository.findByUsernameForUpdate(username).orElseThrow(()-> new UsernameNotFoundException("User not found!"));
        //find if the ticket owner of the ticket is the same and cancel if yes.
        Tickets ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new RuntimeException("Ticket not found."));

        if(!ticket.getUser().getUsername().equals(username)){
            throw new RuntimeException("Ticket Cancellation Failed.");
        }
        //only an active ticket can be cancelled and refunded
        if(ticket.getTicketStatus() != TicketStatus.ACTIVE){
            throw new RuntimeException("Ticket cannot be cancelled.");
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

        cancelTicks.put("message", "Ticket Deleted Successfully!");
        cancelTicks.put("seatNumber", ticket.getSeat().getSeatNumber());

        return cancelTicks;
    }
    public List<MovieTicketsDTO> showMyTickets(Integer userId){
        //ticket statuses are kept up to date by ScheduleService.refreshStatuses()
        List<MovieTicketsDTO> showMytickets = ticketRepository.findTicketByUserUserId(userId);

        return showMytickets;

    }

    public Double showTotalSales(){
        Double totalSales = ticketRepository.showTotalSales();

        return totalSales;
    }
    public Integer showTotalBookings(){
        return ticketRepository.showTotalBookings();
    }


    public List<ShowBookingsResponse> showBookings(){
        List<ShowBookingsResponse> allBookings = ticketRepository.showBookings();

        return allBookings;
    }
    public String findSeatNumber(String ticketCode){
        String seatNumber = ticketRepository.findSeatNumberByTicketCode(ticketCode);

        return seatNumber;
    }

    public Double showRevenueToday(){
        return ticketRepository.showRevenueToday();
    }

    public Map<String, Long> getWeeklySalesTrend() {
        List<ShowBookingsResponse> allBookings = this.showBookings();
        TemporalField weekOfYear = WeekFields.of(Locale.getDefault()).weekOfYear();

        return allBookings.stream()
                .filter(t -> !"CANCELLED".equals(t.getTicketStatus()))
                .collect(Collectors.groupingBy(
                        t -> t.getShowDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString(), // Groups as "Week 11", "Week 12", etc.
                        TreeMap::new,
                        Collectors.counting()
                ));
    }
}
