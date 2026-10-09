package com.poly.payment.dtos.responses;

import java.math.BigDecimal;

public record QrPaymentResponse(
    Long paymentId,
    String paymentCode,
    BigDecimal amount,
    String paymentMethod,
    String qrUrl,
    String bankId,
    String accountNumber,
    String accountName,
    String transferContent,
    String status) {}
