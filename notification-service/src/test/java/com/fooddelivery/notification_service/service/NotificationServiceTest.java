package com.fooddelivery.notification_service.service;

import com.fooddelivery.notification_service.model.Notification;
import com.fooddelivery.notification_service.model.NotificationStatus;
import com.fooddelivery.notification_service.model.NotificationType;
import com.fooddelivery.notification_service.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link NotificationService}.
 *
 * We use @ExtendWith(MockitoExtension.class) so no Spring context is loaded —
 * this keeps the tests fast and truly isolated.
 *
 * Key things we verify:
 *  1. A Notification is first saved with status PENDING.
 *  2. NotificationSender.send() is called with the correct recipient + message.
 *  3. On success the status is updated to SENT (second save).
 *  4. On sender failure the status is updated to FAILED (second save).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Unit Tests")
class NotificationServiceTest {

    // ── Mocks (fakes injected into the real service) ─────────────────────────

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationSender notificationSender;

    // ── System Under Test ─────────────────────────────────────────────────────

    @InjectMocks
    private NotificationService notificationService;

    // ── Reusable test data ────────────────────────────────────────────────────

    private UUID orderId;
    private UUID customerId;
    private String recipient;
    private String message;

    @BeforeEach
    void setUp() {
        orderId    = UUID.randomUUID();
        customerId = UUID.randomUUID();
        recipient  = "user@example.com";
        message    = "Your order is confirmed!";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 1: Happy path — sender succeeds, status becomes SENT
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should save notification as SENT when sender succeeds")
    void createNotification_successfulSend_statusIsSent() {
        // Arrange
        // The repository returns whatever we save the first time (simulate DB assigning an ID)
        Notification savedPending = buildNotification(NotificationStatus.PENDING);
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedPending);

        // Act
        notificationService.createNotification(orderId, customerId, NotificationType.ORDER_RECEIVED, message, recipient);

        // Assert — capture every call to save() so we can inspect them
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());

        // First save must be PENDING
        Notification firstSave = captor.getAllValues().get(0);
        assertThat(firstSave.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(firstSave.getOrderId()).isEqualTo(orderId);
        assertThat(firstSave.getCustomerId()).isEqualTo(customerId);
        assertThat(firstSave.getRecipient()).isEqualTo(recipient);
        assertThat(firstSave.getContent()).isEqualTo(message);
        assertThat(firstSave.getType()).isEqualTo(NotificationType.ORDER_RECEIVED);

        // Second save must be SENT
        Notification secondSave = captor.getAllValues().get(1);
        assertThat(secondSave.getStatus()).isEqualTo(NotificationStatus.SENT);

        // Sender must have been called once with the right args
        verify(notificationSender).send(recipient, message);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 2: Sender throws — status must become FAILED, no exception propagated
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should save notification as FAILED when sender throws")
    void createNotification_senderThrows_statusIsFailed() {
        // Arrange
        Notification savedPending = buildNotification(NotificationStatus.PENDING);
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedPending);

        // Make the sender blow up
        doThrow(new RuntimeException("SMTP connection refused"))
                .when(notificationSender).send(anyString(), anyString());

        // Act — the Exception from the sender must NOT propagate to the caller
        notificationService.createNotification(orderId, customerId, NotificationType.PAYMENT_FAILED, message, recipient);

        // Assert
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());

        Notification secondSave = captor.getAllValues().get(1);
        assertThat(secondSave.getStatus()).isEqualTo(NotificationStatus.FAILED);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 3: Notification fields are correctly populated from the arguments
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should build notification with correct fields before sending")
    void createNotification_buildsNotificationWithCorrectFields() {
        // Arrange
        Notification returned = buildNotification(NotificationStatus.PENDING);
        when(notificationRepository.save(any(Notification.class))).thenReturn(returned);

        // Act
        notificationService.createNotification(orderId, customerId, NotificationType.PAYMENT_SUCCESS, message, recipient);

        // Assert — inspect the very first save call
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, atLeastOnce()).save(captor.capture());

        Notification built = captor.getAllValues().get(0);
        assertThat(built.getOrderId()).isEqualTo(orderId);
        assertThat(built.getCustomerId()).isEqualTo(customerId);
        assertThat(built.getType()).isEqualTo(NotificationType.PAYMENT_SUCCESS);
        assertThat(built.getContent()).isEqualTo(message);
        assertThat(built.getRecipient()).isEqualTo(recipient);
        assertThat(built.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(built.getCreatedAt()).isNotNull();
        assertThat(built.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 4: Repository is ALWAYS called twice (in finally block) regardless of outcome
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should always persist the final status (finally block runs even on failure)")
    void createNotification_alwaysSavesTwice_regardlessOfSenderOutcome() {
        // Arrange
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(buildNotification(NotificationStatus.PENDING));
        doThrow(new RuntimeException("network error"))
                .when(notificationSender).send(anyString(), anyString());

        // Act
        notificationService.createNotification(orderId, customerId, NotificationType.ORDER_RECEIVED, message, recipient);

        // Assert — exactly 2 saves: one before send, one in finally
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Notification buildNotification(NotificationStatus status) {
        return Notification.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .customerId(customerId)
                .type(NotificationType.ORDER_RECEIVED)
                .content(message)
                .recipient(recipient)
                .status(status)
                .createdAt(Instant.now())
                .build();
    }
}
