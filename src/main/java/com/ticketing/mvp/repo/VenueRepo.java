package com.ticketing.mvp.repo;

import com.ticketing.mvp.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepo extends JpaRepository<Venue, Long> {}
