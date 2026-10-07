package com.poly.hotel.repositories;

import com.poly.hotel.models.BookingStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingStatusHistoryRepository
    extends JpaRepository<BookingStatusHistory, Long>,
        JpaSpecificationExecutor<BookingStatusHistory> {
  List<BookingStatusHistory> findByBookingIdAndDelIfFalse(Long bookingId);
}
