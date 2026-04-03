package com.ticketing.mvp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Booking booking;
    @ManyToOne(fetch = FetchType.LAZY)
    private EventSeat eventSeat;
    private BigDecimal price;
}
