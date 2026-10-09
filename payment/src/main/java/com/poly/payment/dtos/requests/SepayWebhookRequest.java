package com.poly.payment.dtos.requests;

import java.math.BigDecimal;

/** SePay webhook payload (https://docs.sepay.vn/tich-hop-webhooks.html). */
public record SepayWebhookRequest(
    Long id,
    String gateway,
    String transactionDate,
    String accountNumber,
    String subAccount,
    String code,
    String content,
    String transferType,
    String description,
    BigDecimal transferAmount,
    BigDecimal accumulated,
    String referenceCode) {}
