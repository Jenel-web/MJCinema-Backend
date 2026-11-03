package com.MovieBookingApplication.MJCinema.DTO;

/**
 * One row of the weekly sales query: the Monday that starts the week (yyyy-MM-dd) and how many tickets
 * were booked for showtimes in that week. Spring Data fills it from the column aliases of a native query.
 */
public interface WeeklySales {
    String getWeekStart();

    Long getBookings();
}
