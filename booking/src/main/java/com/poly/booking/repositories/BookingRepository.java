package com.poly.booking.repositories;

import com.poly.booking.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository
    extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {}
