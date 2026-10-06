package com.poly.payment.dtos.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record PaymentRequest(
    @NotNull Long bookingId,
    @NotNull BigDecimal amount,
    @Size(max = 50) String paymentMethod,
    @Size(max = 255) String transactionId) {}
