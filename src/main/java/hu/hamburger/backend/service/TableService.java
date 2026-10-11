package hu.hamburger.backend.service;

import hu.hamburger.backend.dto.TableAvailabilityDto;
import hu.hamburger.backend.model.RestaurantTable;
import hu.hamburger.backend.model.ReservationStatus;
import hu.hamburger.backend.repository.ReservationRepository;
import hu.hamburger.backend.repository.RestaurantTableRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TableService {

    private final RestaurantTableRepository tableRepository;
    private final ReservationRepository reservationRepository;

    public TableService(RestaurantTableRepository tableRepository, ReservationRepository reservationRepository) {
        this.tableRepository = tableRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<TableAvailabilityDto> getAvailability(LocalDate date, LocalTime startTime) {
        List<RestaurantTable> allTables = tableRepository.findAll();

        Set<Long> occupiedTableIds = reservationRepository
                .findByDateAndStartTime(date, startTime).stream()
                .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
                .map(r -> r.getTable().getId())
                .collect(Collectors.toSet());

        return allTables.stream()
                .map(t -> new TableAvailabilityDto(
                        t.getId(),
                        t.getTableNumber(),
                        t.getCapacity(),
                        !occupiedTableIds.contains(t.getId())))
                .toList();
    }
}