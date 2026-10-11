package hu.hamburger.backend.controller;

import hu.hamburger.backend.dto.TableAvailabilityDto;
import hu.hamburger.backend.model.RestaurantTable;
import hu.hamburger.backend.repository.RestaurantTableRepository;
import hu.hamburger.backend.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/tables")
@CrossOrigin(origins = "http://localhost:5173")
public class TableController {

    @Autowired
    private RestaurantTableRepository tableRepository;

    @Autowired
    private TableService tableService;

    @GetMapping
    public List<RestaurantTable> getAllTables() {
        return tableRepository.findAll();
    }

    @PostMapping
    public RestaurantTable createTable(@RequestBody RestaurantTable table) {
        return tableRepository.save(table);
    }

    @GetMapping("/availability")
    public List<TableAvailabilityDto> getAvailability(
            @RequestParam LocalDate date,
            @RequestParam LocalTime startTime) {
        return tableService.getAvailability(date, startTime);
    }
}