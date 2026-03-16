package com.MovieBookingApplication.MJCinema.Repository;

import com.MovieBookingApplication.MJCinema.DTO.GetUserRequest;
import com.MovieBookingApplication.MJCinema.DTO.ShowUsersResponse;
import com.MovieBookingApplication.MJCinema.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Integer> {


    Optional<Users> findByUsername(String username);

    Optional<Users> findByUsernameAndPassword(String username, String password);

    boolean existsByUsername(String username);
    @Query("SELECT new com.MovieBookingApplication.MJCinema.DTO.GetUserRequest(" +
            "s.username, s.balance) " +
            "FROM Users s "+
            "WHERE s.userId = :userId")//the colon is like a substitute
    GetUserRequest findByUserId(Integer userId);

    @Query("SELECT new com.MovieBookingApplication.MJCinema.DTO.ShowUsersResponse(" +
            "u.username, " +
            "u.balance, " +
            "COUNT(t), " +
            "COALESCE(SUM(sp.price), 0.0)) " +
            "FROM Users u " +
            "LEFT JOIN Tickets t ON t.user = u AND t.ticketStatus != com.MovieBookingApplication.MJCinema.Entity.TicketStatus.CANCELLED " +
            "LEFT JOIN SeatPrice sp ON sp.seatCategory = t.seat.seatCategory AND sp.schedule = t.schedule " +
            "GROUP BY u.username, u.balance")
    List<ShowUsersResponse> findAllUsers();
}
