package com.fooddelivery.notification_service.controller;

import com.fooddelivery.notification_service.model.Notification;
import com.fooddelivery.notification_service.model.NotificationStatus;
import com.fooddelivery.notification_service.model.NotificationType;
import com.fooddelivery.notification_service.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice test for {@link NotificationController}.
 *
 * @WebMvcTest loads only the web layer (controller + MockMvc). The repository
 * is replaced with a @MockBean — no database involved.
 *
 * What we verify:
 *  1. GET /notifications returns all notifications as JSON.
 *  2. GET /notifications/customer/{id} returns only that customer's items.
 *  3. GET /notifications/order/{id} returns only that order's items.
 *  4. Empty results return HTTP 200 with an empty array (not 404).
 */
@WebMvcTest(NotificationController.class)
@DisplayName("NotificationController Web Layer Tests")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationRepository notificationRepository;

    private final UUID customerId = UUID.randomUUID();
    private final UUID orderId    = UUID.randomUUID();

    // ─────────────────────────────────────────────────────────────────────────
    // Test 1: GET /notifications → 200 with full list
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /notifications should return all notifications with 200")
    void getAllNotifications_returnsListWith200() throws Exception {
        // Arrange
        Notification n1 = buildNotification(orderId, customerId, NotificationType.ORDER_RECEIVED);
        Notification n2 = buildNotification(UUID.randomUUID(), UUID.randomUUID(), NotificationType.PAYMENT_SUCCESS);

        when(notificationRepository.findAll()).thenReturn(List.of(n1, n2));

        // Act & Assert
        mockMvc.perform(get("/notifications").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].type").value("ORDER_RECEIVED"))
                .andExpect(jsonPath("$[1].type").value("PAYMENT_SUCCESS"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 2: GET /notifications/customer/{id}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /notifications/customer/{id} should return that customer's notifications")
    void getNotificationsByCustomer_returnsMatchingNotifications() throws Exception {
        // Arrange
        Notification n = buildNotification(orderId, customerId, NotificationType.ORDER_RECEIVED);
        when(notificationRepository.findByCustomerId(customerId)).thenReturn(List.of(n));

        // Act & Assert
        mockMvc.perform(get("/notifications/customer/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customerId").value(customerId.toString()))
                .andExpect(jsonPath("$[0].status").value("SENT"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 3: GET /notifications/order/{id}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /notifications/order/{id} should return that order's notifications")
    void getNotificationsByOrder_returnsMatchingNotifications() throws Exception {
        // Arrange
        Notification n = buildNotification(orderId, customerId, NotificationType.PAYMENT_SUCCESS);
        when(notificationRepository.findByOrderId(orderId)).thenReturn(List.of(n));

        // Act & Assert
        mockMvc.perform(get("/notifications/order/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderId").value(orderId.toString()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test 4: Empty result → 200 with []
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should return empty array (not 404) when no notifications match")
    void getNotificationsByCustomer_noResults_returns200WithEmptyArray() throws Exception {
        UUID unknownId = UUID.randomUUID();
        when(notificationRepository.findByCustomerId(unknownId)).thenReturn(List.of());

        mockMvc.perform(get("/notifications/customer/{id}", unknownId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Notification buildNotification(UUID orderId, UUID customerId, NotificationType type) {
        return Notification.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .customerId(customerId)
                .type(type)
                .content("Test notification")
                .recipient("test@example.com")
                .status(NotificationStatus.SENT)
                .createdAt(Instant.now())
                .build();
    }
}
