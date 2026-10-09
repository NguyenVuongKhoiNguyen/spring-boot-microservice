package com.poly.payment.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.poly.payment.dtos.requests.QrPaymentRequest;
import com.poly.payment.dtos.requests.SepayWebhookRequest;
import com.poly.payment.dtos.responses.PaymentStatusResponse;
import com.poly.payment.dtos.responses.QrPaymentResponse;
import com.poly.payment.dtos.responses.SepayWebhookResponse;
import com.poly.payment.mappers.BookingPaymentMapper;
import com.poly.payment.models.BookingPayment;
import com.poly.payment.models.PaymentStatus;
import com.poly.payment.models.PaymentWebhookEvent;
import com.poly.payment.models.WebhookResult;
import com.poly.payment.repositories.BookingPaymentRepository;
import com.poly.payment.repositories.PaymentWebhookEventRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class QrPaymentServiceTest {

  @Mock private BookingPaymentRepository paymentRepository;
  @Mock private PaymentWebhookEventRepository webhookRepository;
  @Mock private BookingPaymentMapper mapper;
  @InjectMocks private QrPaymentService service;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(service, "accountNumber", "12345");
    ReflectionTestUtils.setField(service, "webhookApiKey", "secret-key");
    ReflectionTestUtils.setField(service, "bankId", "BIDV");
    ReflectionTestUtils.setField(service, "accountName", "TEST ACCOUNT");
    ReflectionTestUtils.setField(service, "paymentCodePrefix", "BKG");
    ReflectionTestUtils.setField(service, "qrTemplate", "compact2");
  }

  @Nested
  @DisplayName("Validation and business rules")
  class BusinessRules {

    @Test
    void createQrPayment_whenAmountHasFraction_throwsBadRequest() {
      QrPaymentRequest request = new QrPaymentRequest(1L, new BigDecimal("100.50"));
      assertThatThrownBy(() -> service.createQrPayment(request))
          .isInstanceOf(ResponseStatusException.class)
          .hasMessageContaining("Amount must be a positive whole number");
    }

    @Test
    void createQrPayment_whenValid_savesAndReturnsResponse() {
      QrPaymentRequest request = new QrPaymentRequest(1L, new BigDecimal("100000"));
      when(paymentRepository.existsByPaymentCodeAndDelIfFalse(any())).thenReturn(false);

      BookingPayment payment =
          BookingPayment.builder()
              .id(1L)
              .amount(new BigDecimal("100000"))
              .paymentCode("BKG123")
              .build();
      when(paymentRepository.save(any(BookingPayment.class))).thenReturn(payment);

      QrPaymentResponse expectedResponse =
          new QrPaymentResponse(
              1L,
              "BKG123",
              new BigDecimal("100000"),
              "VIETQR",
              "url",
              "BIDV",
              "12345",
              "TEST ACCOUNT",
              "BKG123",
              "PENDING");
      when(mapper.toQrResponse(eq(payment), any(), eq("BIDV"), eq("12345"), eq("TEST ACCOUNT")))
          .thenReturn(expectedResponse);

      QrPaymentResponse response = service.createQrPayment(request);

      assertThat(response).isNotNull();
      assertThat(response.paymentId()).isEqualTo(1L);
      verify(paymentRepository).save(any(BookingPayment.class));
    }
  }

  @Nested
  @DisplayName("Errors and edge cases")
  class ErrorsAndEdgeCases {

    @Test
    void getPaymentStatus_whenNotFound_throwsNotFoundException() {
      when(paymentRepository.findByIdAndDelIfFalse(1L)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> service.getPaymentStatus(1L))
          .isInstanceOf(ResponseStatusException.class)
          .hasMessageContaining("Payment not found");
    }

    @Test
    void getPaymentStatus_whenExists_returnsResponse() {
      BookingPayment payment = BookingPayment.builder().id(1L).status("PENDING").build();
      when(paymentRepository.findByIdAndDelIfFalse(1L)).thenReturn(Optional.of(payment));
      when(mapper.toStatusResponse(payment))
          .thenReturn(new PaymentStatusResponse(1L, "PENDING", null));

      PaymentStatusResponse response = service.getPaymentStatus(1L);

      assertThat(response.id()).isEqualTo(1L);
      assertThat(response.status()).isEqualTo("PENDING");
    }

    @Test
    void handleWebhook_whenInvalidAuthHeader_throwsUnauthorized() {
      SepayWebhookRequest request =
          new SepayWebhookRequest(
              1L,
              "gateway",
              "date",
              "acc",
              "sub",
              "code",
              "content",
              "in",
              "desc",
              new BigDecimal("100"),
              new BigDecimal("100"),
              "ref");
      assertThatThrownBy(() -> service.handleWebhook(request, "Apikey wrong-key"))
          .isInstanceOf(ResponseStatusException.class)
          .hasMessageContaining("Invalid or missing API key");
    }

    @Test
    void handleWebhook_whenTransferTypeNotIn_savesIgnoredAndReturnsSuccess() {
      SepayWebhookRequest request =
          new SepayWebhookRequest(
              1L,
              "gateway",
              "date",
              "acc",
              "sub",
              "code",
              "content",
              "out",
              "desc",
              new BigDecimal("100"),
              new BigDecimal("100"),
              "ref");

      SepayWebhookResponse response = service.handleWebhook(request, "Apikey secret-key");

      assertThat(response.success()).isTrue();
      ArgumentCaptor<PaymentWebhookEvent> captor =
          ArgumentCaptor.forClass(PaymentWebhookEvent.class);
      verify(webhookRepository).save(captor.capture());
      assertThat(captor.getValue().getResult()).isEqualTo(WebhookResult.IGNORED);
    }

    @Test
    void handleWebhook_whenDuplicateTransaction_savesDuplicateAndReturnsSuccess() {
      SepayWebhookRequest request =
          new SepayWebhookRequest(
              1L,
              "gateway",
              "date",
              "acc",
              "sub",
              "code",
              "content",
              "in",
              "desc",
              new BigDecimal("100"),
              new BigDecimal("100"),
              "ref");
      when(webhookRepository.existsBySepayTransactionIdAndResultNot(1L, WebhookResult.DUPLICATE))
          .thenReturn(true);

      SepayWebhookResponse response = service.handleWebhook(request, "Apikey secret-key");

      assertThat(response.success()).isTrue();
      ArgumentCaptor<PaymentWebhookEvent> captor =
          ArgumentCaptor.forClass(PaymentWebhookEvent.class);
      verify(webhookRepository).save(captor.capture());
      assertThat(captor.getValue().getResult()).isEqualTo(WebhookResult.DUPLICATE);
    }

    @Test
    void handleWebhook_whenCodeNotFound_savesUnmatchedAndReturnsSuccess() {
      SepayWebhookRequest request =
          new SepayWebhookRequest(
              1L,
              "gateway",
              "date",
              "acc",
              "sub",
              "BKG123",
              "content",
              "in",
              "desc",
              new BigDecimal("100"),
              new BigDecimal("100"),
              "ref");
      when(webhookRepository.existsBySepayTransactionIdAndResultNot(1L, WebhookResult.DUPLICATE))
          .thenReturn(false);
      when(paymentRepository.findByPaymentCodeAndDelIfFalse("BKG123")).thenReturn(Optional.empty());

      SepayWebhookResponse response = service.handleWebhook(request, "Apikey secret-key");

      assertThat(response.success()).isTrue();
      ArgumentCaptor<PaymentWebhookEvent> captor =
          ArgumentCaptor.forClass(PaymentWebhookEvent.class);
      verify(webhookRepository).save(captor.capture());
      assertThat(captor.getValue().getResult()).isEqualTo(WebhookResult.UNMATCHED);
    }

    @Test
    void handleWebhook_whenAmountMismatch_savesAmountMismatchAndReturnsSuccess() {
      SepayWebhookRequest request =
          new SepayWebhookRequest(
              1L,
              "gateway",
              "date",
              "acc",
              "sub",
              "BKG123",
              "content",
              "in",
              "desc",
              new BigDecimal("100"),
              new BigDecimal("100"),
              "ref");
      when(webhookRepository.existsBySepayTransactionIdAndResultNot(1L, WebhookResult.DUPLICATE))
          .thenReturn(false);
      BookingPayment payment =
          BookingPayment.builder().id(2L).amount(new BigDecimal("200")).build();
      when(paymentRepository.findByPaymentCodeAndDelIfFalse("BKG123"))
          .thenReturn(Optional.of(payment));

      SepayWebhookResponse response = service.handleWebhook(request, "Apikey secret-key");

      assertThat(response.success()).isTrue();
      ArgumentCaptor<PaymentWebhookEvent> captor =
          ArgumentCaptor.forClass(PaymentWebhookEvent.class);
      verify(webhookRepository).save(captor.capture());
      assertThat(captor.getValue().getResult()).isEqualTo(WebhookResult.AMOUNT_MISMATCH);
    }

    @Test
    void handleWebhook_whenStatusNotPending_savesDuplicateAndReturnsSuccess() {
      SepayWebhookRequest request =
          new SepayWebhookRequest(
              1L,
              "gateway",
              "date",
              "acc",
              "sub",
              "BKG123",
              "content",
              "in",
              "desc",
              new BigDecimal("100"),
              new BigDecimal("100"),
              "ref");
      when(webhookRepository.existsBySepayTransactionIdAndResultNot(1L, WebhookResult.DUPLICATE))
          .thenReturn(false);
      BookingPayment payment =
          BookingPayment.builder()
              .id(2L)
              .amount(new BigDecimal("100"))
              .status(PaymentStatus.PAID.name())
              .build();
      when(paymentRepository.findByPaymentCodeAndDelIfFalse("BKG123"))
          .thenReturn(Optional.of(payment));

      SepayWebhookResponse response = service.handleWebhook(request, "Apikey secret-key");

      assertThat(response.success()).isTrue();
      ArgumentCaptor<PaymentWebhookEvent> captor =
          ArgumentCaptor.forClass(PaymentWebhookEvent.class);
      verify(webhookRepository).save(captor.capture());
      assertThat(captor.getValue().getResult()).isEqualTo(WebhookResult.DUPLICATE);
    }

    @Test
    void handleWebhook_whenValid_updatesPaymentAndSavesMatched() {
      SepayWebhookRequest request =
          new SepayWebhookRequest(
              1L,
              "gateway",
              "date",
              "acc",
              "sub",
              "BKG123",
              "content",
              "in",
              "desc",
              new BigDecimal("100"),
              new BigDecimal("100"),
              "ref");
      when(webhookRepository.existsBySepayTransactionIdAndResultNot(1L, WebhookResult.DUPLICATE))
          .thenReturn(false);
      BookingPayment payment =
          BookingPayment.builder()
              .id(2L)
              .amount(new BigDecimal("100"))
              .status(PaymentStatus.PENDING.name())
              .build();
      when(paymentRepository.findByPaymentCodeAndDelIfFalse("BKG123"))
          .thenReturn(Optional.of(payment));

      SepayWebhookResponse response = service.handleWebhook(request, "Apikey secret-key");

      assertThat(response.success()).isTrue();
      assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID.name());
      verify(paymentRepository).save(payment);

      ArgumentCaptor<PaymentWebhookEvent> captor =
          ArgumentCaptor.forClass(PaymentWebhookEvent.class);
      verify(webhookRepository).save(captor.capture());
      assertThat(captor.getValue().getResult()).isEqualTo(WebhookResult.MATCHED);
      assertThat(captor.getValue().getPaymentId()).isEqualTo(2L);
    }
  }
}
