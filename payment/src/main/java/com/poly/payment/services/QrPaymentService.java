package com.poly.payment.services;

import com.poly.payment.dtos.requests.QrPaymentRequest;
import com.poly.payment.dtos.requests.SepayWebhookRequest;
import com.poly.payment.dtos.responses.PaymentStatusResponse;
import com.poly.payment.dtos.responses.QrPaymentResponse;
import com.poly.payment.dtos.responses.SepayWebhookResponse;
import com.poly.payment.mappers.BookingPaymentMapper;
import com.poly.payment.models.BookingPayment;
import com.poly.payment.models.PaymentMethod;
import com.poly.payment.models.PaymentStatus;
import com.poly.payment.models.PaymentWebhookEvent;
import com.poly.payment.models.WebhookResult;
import com.poly.payment.repositories.BookingPaymentRepository;
import com.poly.payment.repositories.PaymentWebhookEventRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class QrPaymentService {
  private final BookingPaymentRepository paymentRepository;
  private final PaymentWebhookEventRepository webhookRepository;
  private final BookingPaymentMapper mapper;

  @Value("${sepay.account.number}")
  private String accountNumber;

  @Value("${sepay.webhook.api.key}")
  private String webhookApiKey;

  @Value("${sepay.bank.id}")
  private String bankId;

  @Value("${sepay.account.name}")
  private String accountName;

  @Value("${payment.code.prefix:BKG}")
  private String paymentCodePrefix;

  @Value("${payment.qr.template:compact2}")
  private String qrTemplate;

  @Transactional
  public QrPaymentResponse createQrPayment(QrPaymentRequest request) {
    if (request.amount().remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Amount must be a positive whole number");
    }

    String paymentCode =
        paymentCodePrefix
            + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

    while (paymentRepository.existsByPaymentCodeAndDelIfFalse(paymentCode)) {
      paymentCode =
          paymentCodePrefix
              + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    BookingPayment payment =
        BookingPayment.builder()
            .bookingId(request.bookingId())
            .amount(request.amount())
            .paymentMethod(PaymentMethod.VIETQR.name())
            .status(PaymentStatus.PENDING.name())
            .paymentCode(paymentCode)
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

    payment = paymentRepository.save(payment);

    String qrUrl =
        String.format(
            "https://img.vietqr.io/image/%s-%s-%s.png?amount=%s&addInfo=%s&accountName=%s",
            bankId,
            accountNumber,
            qrTemplate,
            request.amount().toBigInteger().toString(),
            paymentCode,
            accountName.replace(" ", "%20"));

    return mapper.toQrResponse(payment, qrUrl, bankId, accountNumber, accountName);
  }

  public PaymentStatusResponse getPaymentStatus(Long id) {
    BookingPayment payment =
        paymentRepository
            .findByIdAndDelIfFalse(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    return mapper.toStatusResponse(payment);
  }

  @Transactional
  public SepayWebhookResponse handleWebhook(SepayWebhookRequest request, String authHeader) {
    if (authHeader == null
        || !authHeader.toLowerCase().startsWith("apikey ")
        || !authHeader.substring(7).trim().equals(webhookApiKey)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing API key");
    }

    if (!"in".equalsIgnoreCase(request.transferType())) {
      saveWebhookEvent(request, null, WebhookResult.IGNORED);
      return new SepayWebhookResponse(true);
    }

    if (webhookRepository.existsBySepayTransactionIdAndResultNot(
        request.id(), WebhookResult.DUPLICATE)) {
      saveWebhookEvent(request, null, WebhookResult.DUPLICATE);
      return new SepayWebhookResponse(true);
    }

    String code = request.code();
    if (code == null) {
      saveWebhookEvent(request, null, WebhookResult.UNMATCHED);
      return new SepayWebhookResponse(true);
    }

    Optional<BookingPayment> paymentOpt = paymentRepository.findByPaymentCodeAndDelIfFalse(code);
    if (paymentOpt.isEmpty()) {
      saveWebhookEvent(request, null, WebhookResult.UNMATCHED);
      return new SepayWebhookResponse(true);
    }

    BookingPayment payment = paymentOpt.get();
    if (payment.getAmount().compareTo(request.transferAmount()) != 0) {
      saveWebhookEvent(request, payment.getId(), WebhookResult.AMOUNT_MISMATCH);
      return new SepayWebhookResponse(true);
    }

    if (!PaymentStatus.PENDING.name().equals(payment.getStatus())) {
      saveWebhookEvent(request, payment.getId(), WebhookResult.DUPLICATE);
      return new SepayWebhookResponse(true);
    }

    payment.setStatus(PaymentStatus.PAID.name());
    payment.setPaidAt(Instant.now());
    payment.setTransactionId(String.valueOf(request.id()));
    payment.setUpdatedAt(Instant.now());
    paymentRepository.save(payment);

    saveWebhookEvent(request, payment.getId(), WebhookResult.MATCHED);

    return new SepayWebhookResponse(true);
  }

  private void saveWebhookEvent(SepayWebhookRequest request, Long paymentId, WebhookResult result) {
    PaymentWebhookEvent event =
        PaymentWebhookEvent.builder()
            .sepayTransactionId(request.id())
            .paymentId(paymentId)
            .amount(request.transferAmount())
            .transferType(request.transferType())
            .result(result)
            .createdAt(Instant.now())
            .build();
    webhookRepository.save(event);
  }
}
