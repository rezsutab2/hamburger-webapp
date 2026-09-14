package hu.hamburger.backend.controller;

import hu.hamburger.backend.model.Reservation;
import hu.hamburger.backend.model.ReservationStatus;
import hu.hamburger.backend.model.User;
import hu.hamburger.backend.repository.ReservationRepository;
import hu.hamburger.backend.repository.UserRepository;
import hu.hamburger.backend.service.ReservationService;
import hu.hamburger.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "http://localhost:5173")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Felhasználó nem található"));
    }

    @PostMapping
    public Reservation createReservation(@RequestBody Reservation reservation,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        reservation.setUser(user);
        return reservationService.createReservation(reservation);
    }

    @GetMapping
    public List<Reservation> getMyReservations(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        return reservationService.getReservationsForUser(user.getId());
    }

    @GetMapping("/{id}")
    public Reservation getReservationById(@PathVariable Long id,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        Reservation reservation = reservationService.getReservationById(id);

        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Foglalás nem található: " + id);
        }

        return reservation;
    }

    @DeleteMapping("/{id}")
    public void cancelReservation(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        Reservation reservation = reservationService.getReservationById(id);

        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Foglalás nem található: " + id);
        }

        reservationService.cancelReservation(id);
    }
}