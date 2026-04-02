package com.ticketing.mvp.repo;

import com.ticketing.mvp.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepo extends JpaRepository<Payment, Long> {
    Optional<Payment> findByIdempotencyKey(String key);
}
