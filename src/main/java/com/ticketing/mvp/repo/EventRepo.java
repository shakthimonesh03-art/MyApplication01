package com.ticketing.mvp.repo;

import com.ticketing.mvp.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface EventRepo extends JpaRepository<Event, Long> {
    @Query("""
    select e from Event e
    where (:city is null or lower(e.venue.city) = lower(:city))
      and (:category is null or lower(e.category) = lower(:category))
      and (:fromTime is null or e.startTime >= :fromTime)
    """)
    List<Event> search(@Param("city") String city, @Param("category") String category, @Param("fromTime") Instant fromTime);
}
