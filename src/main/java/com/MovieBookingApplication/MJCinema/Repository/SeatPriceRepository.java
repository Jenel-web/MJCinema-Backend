package com.MovieBookingApplication.MJCinema.Repository;

import com.MovieBookingApplication.MJCinema.DTO.SeatPriceResponse;
import com.MovieBookingApplication.MJCinema.Entity.SeatCategory;
import com.MovieBookingApplication.MJCinema.Entity.SeatPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatPriceRepository extends JpaRepository<SeatPrice, Integer> {

    //finds the seat using seat category and scheduleId because seatCategory links seat and seatPrice.
    Optional<SeatPrice> findBySeatCategoryAndScheduleScheduleId(SeatCategory seatCategory, Integer scheduleID);

    List<SeatPrice> findByScheduleScheduleId(Integer scheduleId);

    @Query("SELECT new com.MovieBookingApplication.MJCinema.DTO.SeatPriceResponse(sp.seatCategory, sp.price) " +
            "FROM SeatPrice sp WHERE sp.schedule.scheduleId = :scheduleId")
    List<SeatPriceResponse> findPricesByScheduleId(Integer scheduleId);
}
