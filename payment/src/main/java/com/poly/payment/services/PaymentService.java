package com.poly.payment.services;

import com.poly.payment.dtos.responses.PageResponse;
import com.poly.payment.dtos.responses.PaymentResponse;
import com.poly.payment.mappers.PaymentMapper;
import com.poly.payment.models.Payment;
import com.poly.payment.repositories.PaymentRepository;
import com.poly.payment.repositories.PaymentSpecification;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {
  private final PaymentRepository repository;
  private final PaymentMapper mapper;

  public PageResponse<PaymentResponse> filterAndPaginate(
      Long id,
      Long bookingId,
      BigDecimal amountFrom,
      BigDecimal amountTo,
      String paymentMethod,
      String status,
      String transactionId,
      Instant paidAtFrom,
      Instant paidAtTo,
      Boolean delIf,
      Instant createdAtFrom,
      Instant createdAtTo,
      Instant updatedAtFrom,
      Instant updatedAtTo,
      Pageable pageable) {
    Page<Payment> page =
        repository.findAll(
            PaymentSpecification.filter(
                id,
                bookingId,
                amountFrom,
                amountTo,
                paymentMethod,
                status,
                transactionId,
                paidAtFrom,
                paidAtTo,
                delIf,
                createdAtFrom,
                createdAtTo,
                updatedAtFrom,
                updatedAtTo),
            pageable);
    return PageResponse.from(page.map(mapper::toResponse));
  }
}
