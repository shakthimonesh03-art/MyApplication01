package com.ticketing.mvp.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class ApiDtos {
    public record RegisterRequest(@NotBlank String name, @Email String email, @Size(min = 8) String password) {}
    public record LoginRequest(@Email String email, @NotBlank String password) {}
    public record AuthResponse(String token) {}

    public record VenueRequest(String name, String city, String address, Integer totalCapacity) {}
    public record SeatInput(String rowLabel, Integer seatNumber, String category, BigDecimal price) {}
    public record EventRequest(String title, String description, String category, Instant startTime, Instant endTime,
                               Long venueId, BigDecimal basePrice, List<SeatInput> seats) {}

    public record HoldRequest(Long eventId, List<Long> seatIds) {}
    public record HoldResponse(String holdToken, Instant expiresAt) {}

    public record BookingCreateRequest(String holdToken) {}
    public record PaymentInitiateRequest(Long bookingId, String provider) {}
    public record PaymentConfirmRequest(Long bookingId, String idempotencyKey, String providerRef, boolean success) {}
}
