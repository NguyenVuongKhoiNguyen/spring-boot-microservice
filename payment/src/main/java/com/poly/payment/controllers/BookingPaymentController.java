package com.poly.payment.controllers;

import com.poly.payment.dtos.responses.BookingPaymentResponse;
import com.poly.payment.dtos.responses.PageResponse;
import com.poly.payment.services.BookingPaymentService;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class BookingPaymentController {
  private final BookingPaymentService service;

  @GetMapping
  public PageResponse<BookingPaymentResponse> filterAndPaginate(
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) Long bookingId,
      @RequestParam(required = false) BigDecimal amountFrom,
      @RequestParam(required = false) BigDecimal amountTo,
      @RequestParam(required = false) String paymentMethod,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String transactionId,
      @RequestParam(required = false) Instant paidAtFrom,
      @RequestParam(required = false) Instant paidAtTo,
      @RequestParam(required = false) Boolean delIf,
      @RequestParam(required = false) Instant createdAtFrom,
      @RequestParam(required = false) Instant createdAtTo,
      @RequestParam(required = false) Instant updatedAtFrom,
      @RequestParam(required = false) Instant updatedAtTo,
      Pageable pageable) {
    return service.filterAndPaginate(
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
        updatedAtTo,
        pageable);
  }
}
