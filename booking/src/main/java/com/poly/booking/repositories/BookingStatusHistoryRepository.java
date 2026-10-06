package com.poly.booking.repositories;

import com.poly.booking.models.BookingStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingStatusHistoryRepository
    extends JpaRepository<BookingStatusHistory, Long>,
        JpaSpecificationExecutor<BookingStatusHistory> {}
