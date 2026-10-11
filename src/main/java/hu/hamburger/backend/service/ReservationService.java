package hu.hamburger.backend.service;

import hu.hamburger.backend.exception.ResourceNotFoundException;
import hu.hamburger.backend.model.Reservation;
import hu.hamburger.backend.model.ReservationStatus;
import hu.hamburger.backend.model.RestaurantTable;
import hu.hamburger.backend.repository.ReservationRepository;
import hu.hamburger.backend.repository.RestaurantTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RestaurantTableRepository tableRepository;

    public Reservation createReservation(Reservation reservation) {
        Long tableId = (reservation.getTable() != null) ? reservation.getTable().getId() : null;
        RestaurantTable table = (tableId != null)
                ? tableRepository.findById(tableId)
                  .orElseThrow(() -> new ResourceNotFoundException("Asztal nem található: " + tableId))
                : null;

        if (table == null) {
            throw new ResourceNotFoundException("Asztal nem található.");
        }
        reservation.setTable(table); //a teljes, DB-ből betöltött entitást használjuk tovább

        boolean tableTaken = reservationRepository
                .findByTableAndDateAndStartTime(
                        reservation.getTable(),
                        reservation.getDate(),
                        reservation.getStartTime())
                .stream()
                .anyMatch(existing -> existing.getStatus() != ReservationStatus.CANCELLED);

        if (tableTaken) {
            throw new IllegalStateException("Ez az asztal már foglalt ebben az időpontban.");
        }

        boolean userAlreadyBooked = reservationRepository
                .findByUserAndDateAndStartTime(
                        reservation.getUser(),
                        reservation.getDate(),
                        reservation.getStartTime())
                .stream()
                .anyMatch(existing -> existing.getStatus() != ReservationStatus.CANCELLED);

        if (userAlreadyBooked) {
            throw new IllegalStateException("Már van foglalásod erre az időpontra.");
        }

        reservation.setStatus(ReservationStatus.PENDING);
        return reservationRepository.save(reservation);
    }

    public List<Reservation> getReservationsForUser(Long userId) {
        return reservationRepository.findByUserId(userId);
    }

    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Foglalás nem található: " + id));
    }

    public void cancelReservation(Long id) {
        Reservation reservation = getReservationById(id);
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public Reservation updateStatus(Long id, ReservationStatus status) {
        Reservation reservation = getReservationById(id);
        reservation.setStatus(status);
        return reservationRepository.save(reservation);
    }
}