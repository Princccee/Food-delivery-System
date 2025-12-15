package com.fooddelivery.order_service.controller;

import com.fooddelivery.order_service.DTO.PlaceOrderRequest;
import com.fooddelivery.order_service.DTO.OrderResponse;
import com.fooddelivery.order_service.order.OrderStatus;
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
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Place order - CUSTOMER only
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            Authentication authentication,
            @Valid @RequestBody PlaceOrderRequest request
    ) {
        String email = authentication.getName(); // subject in JWT
        OrderResponse resp = orderService.placeOrder(email, request);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    // customer orders
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getByCustomer(@PathVariable UUID customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    // restaurant owner view
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<OrderResponse>> getByRestaurant(@PathVariable UUID restaurantId) {
        return ResponseEntity.ok(orderService.getOrdersByRestaurant(restaurantId));
    }

    // restaurant owner updates status
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable UUID orderId,
            @RequestParam OrderStatus status
    ) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }

    // in your existing OrderController
    @PostMapping("/api/orders/{orderId}/payment-callback")
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
