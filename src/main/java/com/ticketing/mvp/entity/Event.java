package com.ticketing.mvp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "events")
public class Event {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private String category;
    private String bannerUrl;
    private Instant startTime;
    private Instant endTime;
    private String status;
    private Long organizerId;
    @ManyToOne(fetch = FetchType.LAZY)
    private Venue venue;
    private BigDecimal basePrice;
}
