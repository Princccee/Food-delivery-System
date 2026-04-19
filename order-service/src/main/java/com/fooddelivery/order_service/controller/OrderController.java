package com.fooddelivery.order_service.controller;

import com.fooddelivery.order_service.DTO.PlaceOrderRequest;
import com.fooddelivery.order_service.DTO.OrderResponse;
import com.fooddelivery.order_service.order.OrderStatus;
import com.fooddelivery.order_service.order.PaymentStatus;
import com.fooddelivery.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Place order - CUSTOMER only
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            Authentication authentication,
            @Valid @RequestBody PlaceOrderRequest request
    ) {
        // Get the real UUID from the token credentials
        UUID userId = UUID.fromString(authentication.getCredentials().toString());
        OrderResponse resp = orderService.placeOrder(userId, request);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    // customer orders
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getByCustomer(
            Authentication authentication,
            @PathVariable UUID customerId) {

        UUID loggedInUserId = UUID.fromString(authentication.getCredentials().toString());
        if (!loggedInUserId.equals(customerId)) {
            return ResponseEntity.status(403).build(); // Block unauthorized access
        }
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    // restaurant owner view
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<OrderResponse>> getByRestaurant(@PathVariable UUID restaurantId) {
        return ResponseEntity.ok(orderService.getOrdersByRestaurant(restaurantId));
    }

    @PostMapping("/{orderId}/payment-callback")
    public ResponseEntity<?> paymentCallback(@PathVariable UUID orderId, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if ("SUCCESS".equalsIgnoreCase(status)) {
            orderService.markPaid(orderId); // implement this: set paymentStatus=PAID and update order status
        } else {
            orderService.markPaymentFailed(orderId); // implement accordingly
        }
        return ResponseEntity.ok().build();
    }

}
