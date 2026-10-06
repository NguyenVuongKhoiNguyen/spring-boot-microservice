package com.poly.payment.dtos.responses;

import com.poly.payment.models.Payment;
import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
    Long id,
    Long bookingId,
    BigDecimal amount,
    String paymentMethod,
    String status,
    String transactionId,
    Instant paidAt) {
  public static PaymentResponse from(Payment payment) {
    if (payment == null) {
      return null;
    }
    return new PaymentResponse(
        payment.getId(),
        payment.getBookingId(),
        payment.getAmount(),
        payment.getPaymentMethod(),
        payment.getStatus(),
        payment.getTransactionId(),
        payment.getPaidAt());
  }
}
