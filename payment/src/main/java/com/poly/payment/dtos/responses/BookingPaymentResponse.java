package com.poly.payment.dtos.responses;

import com.poly.payment.models.BookingPayment;
import java.math.BigDecimal;
import java.time.Instant;

public record BookingPaymentResponse(
    Long id,
    Long bookingId,
    BigDecimal amount,
    String paymentMethod,
    String status,
    String transactionId,
    Instant paidAt) {
  public static BookingPaymentResponse from(BookingPayment payment) {
    if (payment == null) {
      return null;
    }
    return new BookingPaymentResponse(
        payment.getId(),
        payment.getBookingId(),
        payment.getAmount(),
        payment.getPaymentMethod(),
        payment.getStatus(),
        payment.getTransactionId(),
        payment.getPaidAt());
  }
}
