package com.MovieBookingApplication.MJCinema.Services;

import com.MovieBookingApplication.MJCinema.DTO.*;
import com.MovieBookingApplication.MJCinema.Entity.*;
import com.MovieBookingApplication.MJCinema.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.fasterxml.jackson.databind.type.LogicalType.Map;

@Service
public class    ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private SeatRespository seatRespository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private SeatPriceRepository seatPriceRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private CinemaRepository cinemaRepository;

    @Autowired
    private UserRepository userRepository;

    public List<ShowingSchedResponse> owShowing() {
        return scheduleRepository.FindNowShowingSchedules();
    }

    public void updateSched() {
        List<Schedule> schedules = scheduleRepository.findAll();
        Set<Movie> schedulesMovies = new HashSet<>();
        LocalDate now = LocalDate.now();
        LocalTime timeNow = LocalTime.now();
        List<Movie> allMovies = movieRepository.findAll();

        for (Movie m : allMovies) {
            Optional<Schedule> nextSchedule = scheduleRepository.findNextSchedule(m, now, timeNow);
            if (nextSchedule.isPresent()) {
                //if you used isPresent, always remember to use get before getting the property
                Long daysBeforeMovie = ChronoUnit.DAYS.between(now, nextSchedule.get().getShowDate());
                if (daysBeforeMovie <= 5) { //IF 5 days or less
                    m.setStatus(MovieStatus.NOW_SHOWING); //make it now showing
                    // add to set
                }//this already checks all the possible schedule
               else{
                   m.setStatus(MovieStatus.COMING_SOON);
                } // if more than 5, its automatically coming soon
            }else{
                    m.setStatus(MovieStatus.INACTIVE);
                }
            schedulesMovies.add(m);
        }
        movieRepository.saveAll(schedulesMovies);
    }

    public List<MovieDetailsDTO> nowShowing() {
        updateStatus();
        updateSched();



        return movieRepository.findByStatus(MovieStatus.NOW_SHOWING).stream()
                .map(m -> new MovieDetailsDTO(m.getPoster(),
                        m.getTitle(), m.getRating(), m.getOverview(), m.getReleaseDate()
                ,m.getMovieId())).toList();

}
    public List<ComingSoonResponse> comingSoon(){
        updateSched();
        updateStatus();
        // now showing list
        List<Movie> comingSoonMovies = movieRepository.findByStatus(MovieStatus.COMING_SOON);
        List<ComingSoonResponse> comingSoonResponses = new ArrayList<>();
        ;
        LocalDate now = LocalDate.now();
        LocalTime timeNow = LocalTime.now();
        for(Movie m: comingSoonMovies){
                Optional<Schedule> schedule = scheduleRepository.findNextSchedule(m, now, timeNow);

                if (schedule.isPresent()) {

                    comingSoonResponses.add(new ComingSoonResponse(
                            m.getPoster(),
                            m.getTitle(),
                            m.getRating(),
                            m.getOverview(),
                            m.getReleaseDate(),
                            schedule.get().getShowDate(),
                            m.getMovieId()
                    ));
                }
            }
        return comingSoonResponses;
    }

    public void updateStatus(){
        LocalDate now = LocalDate.now();
        List<Schedule> beforeDateNow = scheduleRepository.findDoneSchedules(now);
        Set<Schedule> scheds = new HashSet<>();
        for(Schedule s: beforeDateNow){
            s.setStatus(ScheduleStatus.COMPLETED);
            scheds.add(s);
        }
        scheduleRepository.saveAll(scheds);

    }

    public Map<String, List<ShowAvailableSeatsResponse>> showAvailableSeats(Integer scheduleId){

        //make list of all seats
        List<Seat> cinemaSeats = seatRespository.findByScheduleId(scheduleId);

        //Schedule
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(
                ()-> new RuntimeException("Schedule not found!")
        );


        //make list of taken seats
        List<String> takeSeats = ticketRepository.FindSeatSeatNumberByScheduleId(scheduleId);

        //make a map that groups them by row
        Map<String, List< ShowAvailableSeatsResponse>> map = new TreeMap<>();
        //make a map for price and seats
        Map<SeatCategory, Double > priceList = new HashMap<>();

        //find the seat prices per seat category in the schedule
        List <SeatPrice> seatPrices = seatPriceRepository.findByScheduleScheduleId(scheduleId);
        for(SeatPrice s: seatPrices){
            priceList.put(s.getSeatCategory(),s.getPrice());
        }
        for(Seat seat : cinemaSeats){
            String row = seat.getSeatNumber().substring(0,1);//takes the first character which is a letter.

            ShowAvailableSeatsResponse dto = new ShowAvailableSeatsResponse();
            dto.setSeatNumber(seat.getSeatNumber());
            dto.setSeatCategory(seat.getSeatCategory());
            Double seatPrice = priceList.getOrDefault(dto.getSeatCategory(), 0.0);
            dto.setPrice(seatPrice);
            dto.setAvailable(!takeSeats.contains(seat.getSeatNumber()));

            map.computeIfAbsent(row, k -> new ArrayList<>()).add(dto);
            //essential when dealing with maps.
        }

        return map;
    }

    public String addSchedule(AddScheduleRequest request)
    {
            //check movieId
            Movie movie = movieRepository.findById(request.getMovieId()).orElseThrow(() -> new RuntimeException("Movie cannot be found"));
            //check cinemaId
            Cinema cinema = cinemaRepository.findById(request.getCinemaId()).orElseThrow(()-> new RuntimeException("Cinema cannot be found"));

            //check if there is no same start time and end time on the same cinema
            if(scheduleRepository.existsOverlappingSchedule(request.getCinemaId(),
                    request.getShowDate(),request.getSlot())){
                throw new RuntimeException("Schedule overlapping.");
            }

            if(request.getShowDate().isBefore(LocalDate.now()) || request.getShowDate().isEqual(LocalDate.now()) && request.getSlot().getStartTime().isBefore(LocalTime.now())){
                throw new RuntimeException("Schedule Invalid.");
            }

            //make schedule entity and set the values
            Schedule newSchedule = new Schedule();
            newSchedule.setMovie(movie);
            newSchedule.setCinema(cinema);
            newSchedule.setShowDate(request.getShowDate());
            newSchedule.setStartTime(request.getSlot().getStartTime());
            newSchedule.setEndTime(request.getSlot().getEndTime());
            newSchedule.setSlot(request.getSlot());
            newSchedule.setStatus(ScheduleStatus.ACTIVE);

            //save to repo
            Schedule savedSchedule = scheduleRepository.save(newSchedule);

            //add the seat prices for that schedule
        savePrices(SeatCategory.VIP, request.getVipPrice(), savedSchedule);
        savePrices(SeatCategory.REGULAR, request.getRegPrice(), savedSchedule);
        savePrices(SeatCategory.BALCONY, request.getBalPrice(), savedSchedule);

        //save the movie status.
            movie.setStatus(MovieStatus.NOW_SHOWING);
            movieRepository.save(movie);
            return "Schedule added successfully for " + request.getSlot() + " slot";
    }

    public void savePrices(SeatCategory seatCategory, Double price, Schedule schedule){
        SeatPrice seatPrice = new SeatPrice();
        seatPrice.setPrice(price);
        seatPrice.setSeatCategory(seatCategory);
        seatPrice.setSchedule(schedule);
        seatPriceRepository.save(seatPrice);
    }

    @Transactional
    public String removeSchedule(Integer scheduleId){
        //find the schedule using the schedule id
        Schedule schedule = scheduleRepository.findById(scheduleId).
                orElseThrow(()-> new RuntimeException("Schedule not found."));

        if(schedule.getShowDate().isBefore(LocalDate.now())){
            throw new RuntimeException("Schedule cannot be cancelled.");
        }
        //find the tickets in the schedule id and make a list
        List<Tickets> bookedTickets = ticketRepository.findByScheduleScheduleId(scheduleId);


        //refund the money
        SeatPrice VIPprice = seatPriceRepository.findBySeatCategoryAndScheduleScheduleId(SeatCategory.VIP, scheduleId)
                .orElseThrow(() -> new RuntimeException("Seat price not found."));
        SeatPrice RegPrice = seatPriceRepository.findBySeatCategoryAndScheduleScheduleId(SeatCategory.REGULAR, scheduleId)
                .orElseThrow(() -> new RuntimeException("Seat price not found."));
        SeatPrice BalPrice = seatPriceRepository.findBySeatCategoryAndScheduleScheduleId(SeatCategory.BALCONY, scheduleId)
                .orElseThrow(() -> new RuntimeException("Seat price not found."));
        //change the status to cancelled
        for(Tickets t: bookedTickets){
            Users user = t.getUser();
            Double balance = user.getBalance();
            if(t.getSeat().getSeatCategory().equals(SeatCategory.VIP)){
                balance = balance + VIPprice.getPrice();
            }
            else if(t.getSeat().getSeatCategory().equals(SeatCategory.REGULAR)){
                balance = balance + RegPrice.getPrice();
            }
            else{
                balance = balance + BalPrice.getPrice();
            }
            user.setBalance(balance);
            userRepository.save(user);
        }
        //change the status to cancelled
        schedule.setStatus(ScheduleStatus.CANCELLED);
        scheduleRepository.save(schedule);

        return "Schedule cancelled";
    }

}
