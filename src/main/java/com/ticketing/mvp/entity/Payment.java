package com.ticketing.mvp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;


@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "idempotencyKey"))
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Booking booking;
    private String provider;
    private String providerRef;
    @Enumerated(EnumType.STRING)
    private Enums.PaymentStatus status;
    private BigDecimal amount;
    private String idempotencyKey;
    private Instant createdAt;
}
