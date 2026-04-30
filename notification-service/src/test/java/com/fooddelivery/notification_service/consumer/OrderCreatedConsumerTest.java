package com.fooddelivery.notification_service.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.notification_service.events.OrderCreatedEvent;
import com.fooddelivery.notification_service.model.NotificationType;
import com.fooddelivery.notification_service.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link OrderCreatedConsumer}.
 *
 * We treat the Kafka infrastructure as irrelevant here — we directly call
 * consume(String messageJson) as an ordinary method.
 *
 * What we verify:
 *  1. A valid JSON event triggers NotificationService.createNotification() correctly.
 *  2. The generated message contains the order ID prefix.
 *  3. Falls back to a default email when customerEmail is null.
 *  4. Malformed JSON is swallowed gracefully (no exception propagated).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCreatedConsumer Unit Tests")
class OrderCreatedConsumerTest {

    @Mock
    private NotificationService notificationService;

    // Use a real ObjectMapper — it has no I/O side effects and is safe to keep real.
    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private OrderCreatedConsumer consumer;

    // Inject the real ObjectMapper after @InjectMocks creates the object
    // (Mockito can't inject non-mock fields; we use a constructor-compatible approach via field reflection workaround).
    // Instead, wire it manually in each test via a fresh consumer instance.

    private final UUID orderId     = UUID.randomUUID();
    private final UUID customerId  = UUID.randomUUID();
    private final UUID restaurantId = UUID.randomUUID();

    // ─────────────────────────────────────────────────────────────────────────
    // Test 1: Valid event with email — routes to NotificationService correctly
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should call NotificationService with ORDER_RECEIVED type and correct recipient")
    void consume_validEvent_callsNotificationService() throws JsonProcessingException {
        // Arrange
        OrderCreatedConsumer realConsumer = new OrderCreatedConsumer(notificationService, objectMapper);
        OrderCreatedEvent event = new OrderCreatedEvent(orderId, customerId, "john@example.com", restaurantId, 299.99);
        String json = objectMapper.writeValueAsString(event);

        // Act
        realConsumer.consume(json);

        // Assert
        ArgumentCaptor<String> messageCaptor    = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> recipientCaptor  = ArgumentCaptor.forClass(String.class);

        verify(notificationService).createNotification(
                eq(orderId),
                eq(customerId),
                eq(NotificationType.ORDER_RECEIVED),
                messageCaptor.capture(),
                recipientCaptor.capture()
        );

        // The message must mention the order ID prefix
        assertThat(messageCaptor.getValue()).contains(orderId.toString().substring(0, 8));
        // The recipient must match the event's email
        assertThat(recipientCaptor.getValue()).isEqualTo("john@example.com");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 2: Null email → falls back to default
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should use fallback email when customerEmail is null")
    void consume_nullEmail_usesFallbackRecipient() throws JsonProcessingException {
        // Arrange
        OrderCreatedConsumer realConsumer = new OrderCreatedConsumer(notificationService, objectMapper);
        OrderCreatedEvent event = new OrderCreatedEvent(orderId, customerId, null, restaurantId, 100.0);
        String json = objectMapper.writeValueAsString(event);

        // Act
        realConsumer.consume(json);

        // Assert
        ArgumentCaptor<String> recipientCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationService).createNotification(any(), any(), any(), any(), recipientCaptor.capture());

        assertThat(recipientCaptor.getValue()).isEqualTo("customer@example.com");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 3: Malformed JSON — must not crash the consumer (just logs and returns)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should swallow exception and NOT call NotificationService on bad JSON")
    void consume_malformedJson_doesNotCallService() {
        // Arrange
        OrderCreatedConsumer realConsumer = new OrderCreatedConsumer(notificationService, objectMapper);
        String badJson = "{ this is not valid JSON }}}";

        // Act — must not throw
        realConsumer.consume(badJson);

        // Assert — service was never invoked
        verifyNoInteractions(notificationService);
    }
}
