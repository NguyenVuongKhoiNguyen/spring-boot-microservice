package com.poly.payment.controllers;

import com.poly.payment.dtos.requests.QrPaymentRequest;
import com.poly.payment.dtos.requests.SepayWebhookRequest;
import com.poly.payment.dtos.responses.PaymentStatusResponse;
import com.poly.payment.dtos.responses.QrPaymentResponse;
import com.poly.payment.dtos.responses.SepayWebhookResponse;
import com.poly.payment.services.QrPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
  private final QrPaymentService qrPaymentService;

  @PostMapping("/qr")
  @ResponseStatus(HttpStatus.CREATED)
  public QrPaymentResponse createQrPayment(@Valid @RequestBody QrPaymentRequest request) {
    return qrPaymentService.createQrPayment(request);
  }

  @GetMapping("/{id}/status")
  public PaymentStatusResponse getPaymentStatus(@PathVariable Long id) {
    return qrPaymentService.getPaymentStatus(id);
  }

  @PostMapping("/sepay-webhook")
  public SepayWebhookResponse handleWebhook(
      @RequestBody SepayWebhookRequest request,
      @RequestHeader(value = "Authorization", required = false) String authHeader) {
    return qrPaymentService.handleWebhook(request, authHeader);
  }
}
