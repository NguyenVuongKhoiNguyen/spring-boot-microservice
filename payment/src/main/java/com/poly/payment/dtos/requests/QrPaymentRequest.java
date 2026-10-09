package com.poly.payment.dtos.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record QrPaymentRequest(@NotNull Long bookingId, @NotNull @Positive BigDecimal amount) {}
