package com.poly.payment.repositories;

import com.poly.payment.models.BookingPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingPaymentRepository
    extends JpaRepository<BookingPayment, Long>, JpaSpecificationExecutor<BookingPayment> {}
