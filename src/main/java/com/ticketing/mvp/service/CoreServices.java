package com.ticketing.mvp.service;

import com.ticketing.mvp.dto.ApiDtos.*;
import com.ticketing.mvp.entity.*;
import com.ticketing.mvp.repo.*;
import com.ticketing.mvp.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CoreServices {
    private final UserRepo userRepo;
    private final VenueRepo venueRepo;
    private final EventRepo eventRepo;
    private final EventSeatRepo seatRepo;
    private final BookingRepo bookingRepo;
    private final PaymentRepo paymentRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String register(RegisterRequest req) {
        userRepo.findByEmail(req.email()).ifPresent(u -> { throw new IllegalArgumentException("Email already exists"); });
        AppUser user = AppUser.builder()
                .name(req.name())
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .role(Enums.Role.CUSTOMER)
                .createdAt(Instant.now())
                .build();
        userRepo.save(user);
        return jwtService.generate(user.getId(), user.getRole().name());
    }

    public String login(LoginRequest req) {
        AppUser user = userRepo.findByEmail(req.email()).orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) throw new IllegalArgumentException("Invalid credentials");
        return jwtService.generate(user.getId(), user.getRole().name());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Venue createVenue(VenueRequest req) {
        return venueRepo.save(Venue.builder().name(req.name()).city(req.city()).address(req.address())
                .totalCapacity(req.totalCapacity()).build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Event createEvent(EventRequest req) {
        Venue venue = venueRepo.findById(req.venueId()).orElseThrow();
        Event event = eventRepo.save(Event.builder()
                .title(req.title()).description(req.description()).category(req.category())
                .startTime(req.startTime()).endTime(req.endTime()).venue(venue)
                .status("PUBLISHED").organizerId(currentUserId()).basePrice(req.basePrice()).build());

        for (SeatInput seat : req.seats()) {
            seatRepo.save(EventSeat.builder().event(event).rowLabel(seat.rowLabel()).seatNumber(seat.seatNumber())
                    .category(seat.category()).price(seat.price()).status(Enums.SeatStatus.AVAILABLE).build());
        }
        return event;
    }

    public List<Event> listEvents() { return eventRepo.findAll(); }

    public List<Event> search(String city, String category, Instant fromDate) { return eventRepo.search(city, category, fromDate); }

    public List<EventSeat> seats(Long eventId) { return seatRepo.findByEventId(eventId); }

    @Transactional
    public HoldResponse holdSeats(HoldRequest req) {
        releaseExpiredHolds();
        List<EventSeat> seats = seatRepo.lockByIds(req.seatIds());
        if (seats.size() != req.seatIds().size()) throw new IllegalArgumentException("Some seats not found");

        Instant expiry = Instant.now().plusSeconds(5 * 60);
        String holdToken = UUID.randomUUID().toString();
        Long userId = currentUserId();

        for (EventSeat seat : seats) {
            boolean available = seat.getStatus() == Enums.SeatStatus.AVAILABLE ||
                    (seat.getStatus() == Enums.SeatStatus.HELD && seat.getHoldExpiry() != null && seat.getHoldExpiry().isBefore(Instant.now()));
            if (!available) throw new IllegalStateException("Seat already unavailable: " + seat.getId());
            seat.setStatus(Enums.SeatStatus.HELD);
            seat.setHoldToken(holdToken);
            seat.setHeldByUserId(userId);
            seat.setHoldExpiry(expiry);
        }
        return new HoldResponse(holdToken, expiry);
    }

    @Transactional
    public Booking createBooking(BookingCreateRequest req) {
        Long userId = currentUserId();
        List<EventSeat> heldSeats = seatRepo.findAll().stream().filter(s -> req.holdToken().equals(s.getHoldToken())).toList();
        if (heldSeats.isEmpty()) throw new IllegalArgumentException("Invalid hold token");
        if (heldSeats.stream().anyMatch(s -> !Objects.equals(s.getHeldByUserId(), userId) || s.getHoldExpiry().isBefore(Instant.now())))
            throw new IllegalStateException("Hold expired or not owned by user");

        BigDecimal total = heldSeats.stream().map(EventSeat::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        Booking booking = Booking.builder().userId(userId).event(heldSeats.getFirst().getEvent()).totalAmount(total)
                .status(Enums.BookingStatus.PENDING).holdToken(req.holdToken()).createdAt(Instant.now()).build();

        for (EventSeat seat : heldSeats) {
            booking.getItems().add(BookingItem.builder().booking(booking).eventSeat(seat).price(seat.getPrice()).build());
        }
        return bookingRepo.save(booking);
    }

    @Transactional
    public Payment initiatePayment(PaymentInitiateRequest req) {
        Booking b = bookingRepo.findById(req.bookingId()).orElseThrow();
        b.setStatus(Enums.BookingStatus.PAYMENT_IN_PROGRESS);
        return paymentRepo.save(Payment.builder().booking(b).provider(req.provider()).status(Enums.PaymentStatus.INITIATED)
                .amount(b.getTotalAmount()).createdAt(Instant.now()).idempotencyKey(UUID.randomUUID().toString()).build());
    }

    @Transactional
    public Booking confirmPayment(PaymentConfirmRequest req) {
        Optional<Payment> existing = paymentRepo.findByIdempotencyKey(req.idempotencyKey());
        if (existing.isPresent()) return existing.get().getBooking();

        Booking booking = bookingRepo.findById(req.bookingId()).orElseThrow();
        Payment payment = Payment.builder().booking(booking).provider("RAZORPAY").providerRef(req.providerRef())
                .status(req.success() ? Enums.PaymentStatus.SUCCESS : Enums.PaymentStatus.FAILED)
                .amount(booking.getTotalAmount()).idempotencyKey(req.idempotencyKey()).createdAt(Instant.now()).build();
        paymentRepo.save(payment);

        if (req.success()) {
            booking.setStatus(Enums.BookingStatus.CONFIRMED);
            for (BookingItem i : booking.getItems()) {
                EventSeat seat = i.getEventSeat();
                seat.setStatus(Enums.SeatStatus.BOOKED);
                seat.setHoldToken(null);
                seat.setHoldExpiry(null);
            }
        } else {
            booking.setStatus(Enums.BookingStatus.FAILED);
            releaseHoldByToken(booking.getHoldToken());
        }
        return booking;
    }

    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking b = bookingRepo.findById(bookingId).orElseThrow();
        if (b.getStatus() != Enums.BookingStatus.CONFIRMED && b.getStatus() != Enums.BookingStatus.PENDING)
            throw new IllegalStateException("Booking cannot be cancelled");
        b.setStatus(Enums.BookingStatus.CANCELLED);
        releaseHoldByToken(b.getHoldToken());
        return b;
    }

    public Map<String, String> ticket(Long bookingId) {
        Booking b = bookingRepo.findById(bookingId).orElseThrow();
        if (b.getStatus() != Enums.BookingStatus.CONFIRMED) throw new IllegalStateException("Booking not confirmed");
        return Map.of("bookingId", String.valueOf(b.getId()), "qrCodeText", "TICKET-" + b.getId() + "-" + b.getUserId());
    }

    @Transactional
    public void releaseExpiredHolds() {
        List<EventSeat> expired = seatRepo.findByHoldExpiryBeforeAndStatus(Instant.now(), Enums.SeatStatus.HELD);
        for (EventSeat seat : expired) {
            seat.setStatus(Enums.SeatStatus.AVAILABLE);
            seat.setHoldToken(null);
            seat.setHeldByUserId(null);
            seat.setHoldExpiry(null);
        }
    }

    private void releaseHoldByToken(String token) {
        if (token == null) return;
        for (EventSeat seat : seatRepo.findAll()) {
            if (token.equals(seat.getHoldToken()) && seat.getStatus() == Enums.SeatStatus.HELD) {
                seat.setStatus(Enums.SeatStatus.AVAILABLE);
                seat.setHoldToken(null);
                seat.setHeldByUserId(null);
                seat.setHoldExpiry(null);
            }
        }
    }

    public Long currentUserId() {
        String principal = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Long.valueOf(principal);
    }
}
