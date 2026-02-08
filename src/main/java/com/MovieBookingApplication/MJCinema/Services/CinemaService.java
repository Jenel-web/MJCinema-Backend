package com.MovieBookingApplication.MJCinema.Services;

import com.MovieBookingApplication.MJCinema.Repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CinemaService {

    @Autowired
    private ScheduleRepository scheduleRepository;


}
