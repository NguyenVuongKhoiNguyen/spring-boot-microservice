package com.poly.payment.repositories;

import com.poly.payment.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository
    extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {}
