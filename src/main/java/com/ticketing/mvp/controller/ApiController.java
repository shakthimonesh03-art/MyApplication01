package com.ticketing.mvp.controller;

import com.ticketing.mvp.dto.ApiDtos.*;
import com.ticketing.mvp.entity.Booking;
import com.ticketing.mvp.entity.Event;
import com.ticketing.mvp.entity.EventSeat;
import com.ticketing.mvp.entity.Payment;
import com.ticketing.mvp.entity.Venue;
import com.ticketing.mvp.service.CoreServices;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {
    private final CoreServices core;

    @PostMapping("/auth/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) { return new AuthResponse(core.register(req)); }

    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) { return new AuthResponse(core.login(req)); }

    @GetMapping("/events")
    public List<Event> events() { return core.listEvents(); }

    @GetMapping("/events/search")
    public List<Event> search(@RequestParam(required = false) String city,
                              @RequestParam(required = false) String category,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate) {
        return core.search(city, category, fromDate);
    }

    @GetMapping("/events/{eventId}/seats")
    public List<EventSeat> seats(@PathVariable Long eventId) { return core.seats(eventId); }

    @PostMapping("/inventory/hold")
    public HoldResponse hold(@RequestBody HoldRequest req) { return core.holdSeats(req); }

    @PostMapping("/bookings")
    public Booking createBooking(@RequestBody BookingCreateRequest req) { return core.createBooking(req); }

    @PostMapping("/bookings/{id}/cancel")
    public Booking cancel(@PathVariable Long id) { return core.cancelBooking(id); }

    @GetMapping("/bookings/{id}/ticket")
    public Map<String, String> ticket(@PathVariable Long id) { return core.ticket(id); }

    @PostMapping("/payments/initiate")
    public Payment initiate(@RequestBody PaymentInitiateRequest req) { return core.initiatePayment(req); }

    @PostMapping("/payments/confirm")
    public Booking confirm(@RequestBody PaymentConfirmRequest req) { return core.confirmPayment(req); }

    @PostMapping("/admin/venues")
    public Venue adminVenue(@RequestBody VenueRequest req) { return core.createVenue(req); }

    @PostMapping("/admin/events")
    public Event adminEvent(@RequestBody EventRequest req) { return core.createEvent(req); }
}
