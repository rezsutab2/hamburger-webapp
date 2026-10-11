package hu.hamburger.backend.dto;

public record TableAvailabilityDto(Long id, int tableNumber, int capacity, boolean available) {}