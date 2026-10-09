package com.poly.payment.repositories;

import com.poly.payment.models.BookingPayment;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingPaymentRepository
    extends JpaRepository<BookingPayment, Long>, JpaSpecificationExecutor<BookingPayment> {

  boolean existsByPaymentCodeAndDelIfFalse(String paymentCode);

  Optional<BookingPayment> findByIdAndDelIfFalse(Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<BookingPayment> findByPaymentCodeAndDelIfFalse(String paymentCode);
}
