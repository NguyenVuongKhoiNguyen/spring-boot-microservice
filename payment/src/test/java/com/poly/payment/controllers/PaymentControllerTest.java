package com.poly.payment.controllers;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poly.payment.dtos.requests.QrPaymentRequest;
import com.poly.payment.dtos.requests.SepayWebhookRequest;
import com.poly.payment.models.BookingPayment;
import com.poly.payment.repositories.BookingPaymentRepository;
import com.poly.payment.repositories.PaymentWebhookEventRepository;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PaymentControllerTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine").withDatabaseName("payment");

  @Autowired private MockMvc mockMvc;
  @Autowired private BookingPaymentRepository repository;
  @Autowired private PaymentWebhookEventRepository webhookRepository;
  @Autowired private ObjectMapper objectMapper;

  @Value("${sepay.webhook.api.key}")
  private String webhookApiKey;

  @BeforeEach
  void setUp() {
    webhookRepository.deleteAll();
    repository.deleteAll();
  }

  @Test
  void createQrPayment_whenValid_returnsCreatedAndSavesPayment() throws Exception {
    QrPaymentRequest request = new QrPaymentRequest(1L, new BigDecimal("100000"));

    mockMvc
        .perform(
            post("/api/payments/qr")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.amount").value("100000"))
        .andExpect(jsonPath("$.paymentMethod").value("VIETQR"))
        .andExpect(jsonPath("$.status").value("PENDING"));
  }

  @Test
  void getPaymentStatus_whenExists_returnsStatus() throws Exception {
    BookingPayment payment =
        BookingPayment.builder()
            .bookingId(1L)
            .amount(BigDecimal.TEN)
            .paymentMethod("VIETQR")
            .status("PENDING")
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    repository.save(payment);

    mockMvc
        .perform(get("/api/payments/{id}/status", payment.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PENDING"));
  }

  @Test
  void handleWebhook_whenValid_updatesPaymentToPaid() throws Exception {
    BookingPayment payment =
        BookingPayment.builder()
            .bookingId(1L)
            .amount(new BigDecimal("100000"))
            .paymentMethod("VIETQR")
            .status("PENDING")
            .paymentCode("BKGTEST")
            .delIf(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    repository.save(payment);

    SepayWebhookRequest request =
        new SepayWebhookRequest(
            123L,
            "gateway",
            "date",
            "acc",
            "sub",
            "BKGTEST",
            "content",
            "in",
            "desc",
            new BigDecimal("100000"),
            new BigDecimal("100000"),
            "ref");

    mockMvc
        .perform(
            post("/api/payments/sepay-webhook")
                .header("Authorization", "Apikey " + webhookApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success", is(true)));
  }

  @Test
  void handleWebhook_whenInvalidAuthHeader_returnsUnauthorized() throws Exception {
    SepayWebhookRequest request =
        new SepayWebhookRequest(
            123L,
            "gateway",
            "date",
            "acc",
            "sub",
            "BKGTEST",
            "content",
            "in",
            "desc",
            new BigDecimal("100000"),
            new BigDecimal("100000"),
            "ref");

    mockMvc
        .perform(
            post("/api/payments/sepay-webhook")
                .header("Authorization", "Apikey wrongkey")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }
}
