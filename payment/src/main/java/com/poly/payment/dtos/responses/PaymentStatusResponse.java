package com.poly.payment.dtos.responses;

import java.time.Instant;

public record PaymentStatusResponse(Long id, String status, Instant paidAt) {}
