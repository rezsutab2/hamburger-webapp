package hu.hamburger.backend.repository;

import hu.hamburger.backend.model.Reservation;
import hu.hamburger.backend.model.RestaurantTable;
import hu.hamburger.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserId(Long userId);
    List<Reservation> findByTableAndDateAndStartTime(RestaurantTable table, LocalDate date, LocalTime startTime);
    List<Reservation> findByUserAndDateAndStartTime(User user, LocalDate date, LocalTime startTime);
    List<Reservation> findByDateAndStartTime(LocalDate date, LocalTime startTime);
}