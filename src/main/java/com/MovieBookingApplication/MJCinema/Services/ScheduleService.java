package com.MovieBookingApplication.MJCinema.Services;

import com.MovieBookingApplication.MJCinema.Controllers.MovieController;
import com.MovieBookingApplication.MJCinema.DTO.*;
import com.MovieBookingApplication.MJCinema.Entity.*;
import com.MovieBookingApplication.MJCinema.Repository.*;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
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
    private SeatService seatService;

    @Autowired
    private CinemaRepository cinemaRepository;

    @Autowired
    private UserRepository userRepository;

    public List<ShowingSchedResponse> owShowing() {
        return scheduleRepository.FindNowShowingSchedules();
    }

    public Optional<Schedule> findSchedule(Integer scheduleId){
      return scheduleRepository.findByIdWithDetails(scheduleId);

    }
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ScheduleService.class);

    private static final int NOW_SHOWING_DAYS = 5; //a movie is now showing if its next schedule is within this many days

    //statuses are refreshed by bulk updates on a timer and after schedule changes, not on every read.
    @Scheduled(fixedDelay = 60_000, initialDelay = 5_000)
    public void scheduledRefresh() {
        long start = System.nanoTime();
        refreshStatuses();
        log.info("scheduled status refresh took {} ms", (System.nanoTime() - start) / 1_000_000);
    }

    @Transactional
    public void refreshStatuses() {
        LocalDate today = LocalDate.now();
        LocalTime timeNow = LocalTime.now();
        LocalDate limit = today.plusDays(NOW_SHOWING_DAYS);

        scheduleRepository.completePastSchedules(today, timeNow);
        ticketRepository.completePastTickets(today, timeNow);
        movieRepository.markNowShowing(today, timeNow, limit);
        movieRepository.markComingSoon(today, timeNow, limit);
        movieRepository.markInactive(today, timeNow);
    }

    public List<MovieDetailsDTO> nowShowing() {
        return movieRepository.findByStatus(MovieStatus.NOW_SHOWING).stream()
                .map(m -> new MovieDetailsDTO(m.getPoster(),
                        m.getTitle(), m.getRating(), m.getOverview(), m.getReleaseDate()
                ,m.getMovieId())).toList();

}
    public List<ComingSoonResponse> comingSoon(){
        return movieRepository.findComingSoon(LocalDate.now(), LocalTime.now());
    }

    public Map<String, List<ShowAvailableSeatsResponse>> showAvailableSeats(Integer scheduleId){

        //make list of all seats
        List<Seat> cinemaSeats = seatRespository.findByScheduleId(scheduleId);

        //Schedule only needs to exist, so there is no need to load it
        if(!scheduleRepository.existsById(scheduleId)){
            throw new RuntimeException("Schedule not found!");
        }

        //make a set of taken seats so the lookup below is fast
        Set<String> takeSeats = new HashSet<>(ticketRepository.FindSeatSeatNumberByScheduleId(scheduleId));

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

        //the movie status (now showing / coming soon) is recomputed from its schedules.
        refreshStatuses();
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
        //find the schedule using the schedule id and lock it, so no ticket can be booked while it is cancelled
        Schedule schedule = scheduleRepository.findByIdForUpdate(scheduleId).
                orElseThrow(()-> new RuntimeException("Schedule not found."));

        if(schedule.getShowDate().isBefore(LocalDate.now())
                || schedule.getStatus() == ScheduleStatus.CANCELLED
                || schedule.getStatus() == ScheduleStatus.COMPLETED){
            throw new RuntimeException("Schedule cannot be cancelled.");
        }
        //find the tickets in the schedule id, the user and seat come with them in the same query
        List<Tickets> bookedTickets = ticketRepository.findActiveWithUserAndSeat(scheduleId);

        //the three prices of the schedule in one query
        Map<SeatCategory, Double> prices = new HashMap<>();
        for(SeatPrice sp: seatPriceRepository.findByScheduleScheduleId(scheduleId)){
            prices.put(sp.getSeatCategory(), sp.getPrice());
        }

        //refund the money
        Set<Users> refundedUsers = new HashSet<>();
        for(Tickets t: bookedTickets){
            Double refund = prices.get(t.getSeat().getSeatCategory());
            if(refund == null){
                throw new RuntimeException("Seat price not found.");
            }
            Users user = t.getUser();
            user.setBalance(user.getBalance() + refund);
            refundedUsers.add(user);
            //the money is back, so the ticket is cancelled too (it no longer counts as revenue or blocks the seat)
            t.setTicketStatus(TicketStatus.CANCELLED);
        }
        userRepository.saveAll(refundedUsers);
        ticketRepository.saveAll(bookedTickets);
        //change the status to cancelled
        schedule.setStatus(ScheduleStatus.CANCELLED);
        scheduleRepository.save(schedule);
        refreshStatuses();

        return "Schedule cancelled";
    }

    public List<Schedule> findSchedules(Integer movieId){
        LocalDate now = LocalDate.now(); //gets the day today
        LocalTime timeNow = LocalTime.now(); //time today
        List<Schedule> moviesSchedules = scheduleRepository.findByMovieId(movieId, now, timeNow); // gets the schedules of the movieId
        List<Schedule> showSchedule = new ArrayList<>();
        for(Schedule s: moviesSchedules){
            Long daysBeforeMovie = ChronoUnit.DAYS.between(now, s.getShowDate());

            if(daysBeforeMovie <= 5){
                showSchedule.add(s);
            }
        }

        return showSchedule; //returns the list of now showing schedules
    }

    public List<SeatPriceResponse> findSeatPricesBySchedule(Integer scheduleId){
        return seatPriceRepository.findPricesByScheduleId(scheduleId);
    }

    public List<ShowSeatStatsResponse> showSeatStats(){
        //one grouped query, the percentage is worked out in the response itself.
        return scheduleRepository.findSeatStats();
    }

    public List<BestShowTimeResponse> showBestShowTime(){
        return scheduleRepository.getAverageRevenuePerSlot();
        //returns the average revenue of the five slot to show which showTime more people are more like ly to go
    }

    public Long showScheduleCount(){
        return scheduleRepository.showScheduleCount();
    }

    public Double showAverageRevenue(){
        return scheduleRepository.getAverageRevenuePerSchedule();
    }

    public List<String> showScheduleSlots(){
        List<String> slotValues = new ArrayList<>();

        for(ScheduleSlot slot: ScheduleSlot.values()){
            slotValues.add(slot.name());
        }

        return slotValues;
    }

}
