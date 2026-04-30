package com.fooddelivery.notification_service.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.notification_service.events.PaymentEvent;
import com.fooddelivery.notification_service.model.NotificationType;
import com.fooddelivery.notification_service.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PaymentEventConsumer}.
 *
 * This consumer has richer branching logic than the order consumer:
 * it selects a different NotificationType and message body depending on
 * whether the payment succeeded or failed.
 *
 * What we verify:
 *  1. "SUCCESS" status → PAYMENT_SUCCESS type.
 *  2. "PAID" status (alternative) → also treated as PAYMENT_SUCCESS.
 *  3. "FAILED" status → PAYMENT_FAILED type.
 *  4. Null email → fallback to default.
 *  5. Malformed JSON → exception swallowed, service not called.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentEventConsumer Unit Tests")
class PaymentEventConsumerTest {

    @Mock
    private NotificationService notificationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UUID orderId    = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();

    // ─────────────────────────────────────────────────────────────────────────
    // Test 1: SUCCESS status → PAYMENT_SUCCESS notification
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should create PAYMENT_SUCCESS notification when status is SUCCESS")
    void consume_successStatus_createsPaymentSuccessNotification() throws JsonProcessingException {
        // Arrange
        PaymentEventConsumer consumer = new PaymentEventConsumer(notificationService, objectMapper);
        PaymentEvent event = new PaymentEvent(orderId, customerId, "alice@example.com", "SUCCESS");
        String json = objectMapper.writeValueAsString(event);

        // Act
        consumer.consume(json);

        // Assert
        ArgumentCaptor<NotificationType> typeCaptor    = ArgumentCaptor.forClass(NotificationType.class);
        ArgumentCaptor<String>           messageCaptor = ArgumentCaptor.forClass(String.class);

        verify(notificationService).createNotification(
                eq(orderId),
                eq(customerId),
                typeCaptor.capture(),
                messageCaptor.capture(),
                eq("alice@example.com")
        );

        assertThat(typeCaptor.getValue()).isEqualTo(NotificationType.PAYMENT_SUCCESS);
        assertThat(messageCaptor.getValue()).contains(orderId.toString().substring(0, 8));
        assertThat(messageCaptor.getValue()).containsIgnoringCase("successful");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 2: PAID (alternate success value) → also PAYMENT_SUCCESS
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should treat PAID status as PAYMENT_SUCCESS")
    void consume_paidStatus_createsPaymentSuccessNotification() throws JsonProcessingException {
        PaymentEventConsumer consumer = new PaymentEventConsumer(notificationService, objectMapper);
        PaymentEvent event = new PaymentEvent(orderId, customerId, "bob@example.com", "PAID");
        String json = objectMapper.writeValueAsString(event);

        consumer.consume(json);

        ArgumentCaptor<NotificationType> typeCaptor = ArgumentCaptor.forClass(NotificationType.class);
        verify(notificationService).createNotification(any(), any(), typeCaptor.capture(), any(), any());

        assertThat(typeCaptor.getValue()).isEqualTo(NotificationType.PAYMENT_SUCCESS);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 3: FAILED status → PAYMENT_FAILED notification with failure message
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should create PAYMENT_FAILED notification when status is FAILED")
    void consume_failedStatus_createsPaymentFailedNotification() throws JsonProcessingException {
        PaymentEventConsumer consumer = new PaymentEventConsumer(notificationService, objectMapper);
        PaymentEvent event = new PaymentEvent(orderId, customerId, "carol@example.com", "FAILED");
        String json = objectMapper.writeValueAsString(event);

        consumer.consume(json);

        ArgumentCaptor<NotificationType> typeCaptor    = ArgumentCaptor.forClass(NotificationType.class);
        ArgumentCaptor<String>           messageCaptor = ArgumentCaptor.forClass(String.class);

        verify(notificationService).createNotification(
                eq(orderId), eq(customerId),
                typeCaptor.capture(),
                messageCaptor.capture(),
                eq("carol@example.com")
        );

        assertThat(typeCaptor.getValue()).isEqualTo(NotificationType.PAYMENT_FAILED);
        assertThat(messageCaptor.getValue()).containsIgnoringCase("failed");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 4: Null email → fallback email used
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should use fallback email when customerEmail is null")
    void consume_nullEmail_usesFallbackRecipient() throws JsonProcessingException {
        PaymentEventConsumer consumer = new PaymentEventConsumer(notificationService, objectMapper);
        PaymentEvent event = new PaymentEvent(orderId, customerId, null, "SUCCESS");
        String json = objectMapper.writeValueAsString(event);

        consumer.consume(json);

        ArgumentCaptor<String> recipientCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationService).createNotification(any(), any(), any(), any(), recipientCaptor.capture());

        assertThat(recipientCaptor.getValue()).isEqualTo("customer@example.com");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 5: Malformed JSON → exception swallowed, service never called
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should swallow exception on malformed JSON and NOT call NotificationService")
    void consume_malformedJson_doesNotCallService() {
        PaymentEventConsumer consumer = new PaymentEventConsumer(notificationService, objectMapper);

        // Act — must not throw
        consumer.consume("THIS IS NOT JSON");

        verifyNoInteractions(notificationService);
    }
}
