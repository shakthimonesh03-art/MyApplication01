package com.ticketing.mvp.config;

import com.ticketing.mvp.entity.AppUser;
import com.ticketing.mvp.entity.Enums;
import com.ticketing.mvp.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

@Configuration
@RequiredArgsConstructor
public class BootstrapConfig {
    private final UserRepo userRepo;
    private final PasswordEncoder encoder;

    @Bean
    CommandLineRunner initAdmin() {
        return args -> userRepo.findByEmail("admin@local.test").orElseGet(() -> userRepo.save(AppUser.builder()
                .name("System Admin")
                .email("admin@local.test")
                .passwordHash(encoder.encode("Admin@123"))
                .role(Enums.Role.ADMIN)
                .createdAt(Instant.now())
                .build()));
    }
}
