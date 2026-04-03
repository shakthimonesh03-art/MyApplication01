package com.ticketing.mvp.repo;

import com.ticketing.mvp.entity.Enums;
import com.ticketing.mvp.entity.EventSeat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface EventSeatRepo extends JpaRepository<EventSeat, Long> {
    List<EventSeat> findByEventId(Long eventId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from EventSeat s where s.id in :ids")
    List<EventSeat> lockByIds(@Param("ids") List<Long> ids);

    List<EventSeat> findByHoldExpiryBeforeAndStatus(Instant now, Enums.SeatStatus status);
}
