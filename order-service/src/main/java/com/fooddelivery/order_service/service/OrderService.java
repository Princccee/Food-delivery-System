package com.fooddelivery.order_service.service;

import com.fooddelivery.order_service.DTO.PlaceOrderRequest;
import com.fooddelivery.order_service.DTO.OrderItemResponse;
import com.fooddelivery.order_service.DTO.OrderResponse;
import com.fooddelivery.order_service.events.OrderCreatedEvent;
import com.fooddelivery.order_service.kafka.OrderEventProducer;
import com.fooddelivery.order_service.order.*;
import com.fooddelivery.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate; // bean to configure
    private final String restaurantBaseUrl = "http://localhost:8082"; // or read from config
    private final OrderEventProducer orderEventProducer;

    private UUID userIdFromEmail(String email) {
        return UUID.nameUUIDFromBytes(email.getBytes());
    }

    public OrderResponse placeOrder(String customerEmail, PlaceOrderRequest req) {

        UUID customerId = userIdFromEmail(customerEmail);
        UUID restaurantId = req.getRestaurantId();

        // call restaurant-service to resolve prices
        var menuItems = restTemplate.getForObject(restaurantBaseUrl + "/menu-items/internal/" + restaurantId , com.fasterxml.jackson.databind.JsonNode.class);

        // Convert requested items into OrderItem entities and compute totals
        List<OrderItem> orderItems = req.getItems().stream().map(itemReq -> {
            // find price from menuItems json
            double price = findPriceFromMenuJson(menuItems, itemReq.getMenuItemId());
            double lineTotal = price * itemReq.getQuantity();
            return OrderItem.builder()
                    .menuItemId(itemReq.getMenuItemId())
                    .quantity(itemReq.getQuantity())
                    .price(price)
                    .lineTotal(lineTotal)
                    .build();
        }).collect(Collectors.toList());

        double total = orderItems.stream().mapToDouble(OrderItem::getLineTotal).sum();

        Order order = Order.builder()
                .customerId(customerId)
                .restaurantId(restaurantId)
                .items(orderItems)
                .totalAmount(total)
                .status(OrderStatus.CREATED)
                .paymentStatus(PaymentStatus.PENDING)
                .deliveryAddress(req.getDeliveryAddress())
                .createdAt(Instant.now())
                .build();

        Order saved = orderRepository.save(order);

        // Publish event
        orderEventProducer.publishOrderCreated(
                new OrderCreatedEvent(
                        saved.getId(),
                        saved.getCustomerId(),
                        saved.getRestaurantId(),
                        saved.getTotalAmount()
                )
        );

        return toResponse(saved);
    }

    private double findPriceFromMenuJson(com.fasterxml.jackson.databind.JsonNode menuItems, UUID menuItemId) {
        if (menuItems == null || !menuItems.isArray()) throw new RuntimeException("Could not fetch menu");
        for (com.fasterxml.jackson.databind.JsonNode node : menuItems) {
            if (node.has("id") && node.get("id").asText().equals(menuItemId.toString())) {
                return node.get("price").asDouble();
            }
        }
        throw new RuntimeException("Menu item not found: " + menuItemId);
    }

    public OrderResponse getOrder(UUID id) {
        return orderRepository.findById(id).map(this::toResponse).orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public List<OrderResponse> getOrdersByCustomer(UUID customerId) {
        return orderRepository.findByCustomerId(customerId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByRestaurant(UUID restaurantId) {
        return orderRepository.findByRestaurantId(restaurantId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public OrderResponse updateOrderStatus(UUID orderId, PaymentStatus paymentStatus, OrderStatus newStatus) {
        Order o = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        o.setStatus(newStatus);
        o.setPaymentStatus(paymentStatus);
        o.setUpdatedAt(Instant.now());
        return toResponse(orderRepository.save(o));
    }

    private OrderResponse toResponse(Order o) {
        var items = o.getItems().stream().map(i -> OrderItemResponse.builder()
                .id(i.getId())
                .menuItemId(i.getMenuItemId())
                .quantity(i.getQuantity())
                .price(i.getPrice())
                .lineTotal(i.getLineTotal())
                .build()).collect(Collectors.toList());

        return OrderResponse.builder()
                .id(o.getId())
                .customerId(o.getCustomerId())
                .restaurantId(o.getRestaurantId())
                .items(items)
                .totalAmount(o.getTotalAmount())
                .status(o.getStatus())
                .paymentStatus(o.getPaymentStatus())
                .deliveryAddress(o.getDeliveryAddress())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }

    public void markPaid(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Idempotency: if already paid, ignore
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return;
        }

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setStatus(OrderStatus.CONFIRMED);    // business rule: order becomes “confirmed” on successful payment
        order.setUpdatedAt(Instant.now());

        orderRepository.save(order);
    }


    public void markPaymentFailed(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // If already marked failed, skip
        if (order.getPaymentStatus() == PaymentStatus.FAILED) {
            return;
        }

        order.setPaymentStatus(PaymentStatus.FAILED);

        // Business decision:
        // When payment fails, order should not proceed. Usually marked as CANCELLED.
        order.setStatus(OrderStatus.CANCELLED);

        order.setUpdatedAt(Instant.now());

        orderRepository.save(order);
    }

}
