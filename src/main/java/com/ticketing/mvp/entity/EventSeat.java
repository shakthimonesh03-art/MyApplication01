package com.ticketing.mvp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;


@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "rowLabel", "seatNumber"}))
public class EventSeat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Event event;

    private String rowLabel;
    private Integer seatNumber;
    private String category;
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private Enums.SeatStatus status;
    private String holdToken;
    private Long heldByUserId;
    private Instant holdExpiry;
}
