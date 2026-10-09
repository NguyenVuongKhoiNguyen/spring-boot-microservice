package com.poly.payment.models;

public enum WebhookResult {
  MATCHED,
  UNMATCHED,
  AMOUNT_MISMATCH,
  DUPLICATE,
  IGNORED
}
