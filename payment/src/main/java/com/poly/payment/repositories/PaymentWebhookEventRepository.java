package com.poly.payment.repositories;

import com.poly.payment.models.PaymentWebhookEvent;
import com.poly.payment.models.WebhookResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentWebhookEventRepository extends JpaRepository<PaymentWebhookEvent, Long> {

  boolean existsBySepayTransactionIdAndResultNot(Long sepayTransactionId, WebhookResult result);
}
