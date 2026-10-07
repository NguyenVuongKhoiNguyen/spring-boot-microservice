package com.poly.payment.services;

import com.poly.payment.dtos.responses.BookingPaymentResponse;
import com.poly.payment.dtos.responses.PageResponse;
import com.poly.payment.mappers.BookingPaymentMapper;
import com.poly.payment.models.BookingPayment;
import com.poly.payment.repositories.BookingPaymentRepository;
import com.poly.payment.repositories.BookingPaymentSpecification;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingPaymentService {
  private final BookingPaymentRepository repository;
  private final BookingPaymentMapper mapper;

  public PageResponse<BookingPaymentResponse> filterAndPaginate(
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
    Page<BookingPayment> page =
        repository.findAll(
            BookingPaymentSpecification.filter(
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
