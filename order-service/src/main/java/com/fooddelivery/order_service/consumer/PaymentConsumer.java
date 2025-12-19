package com.fooddelivery.order_service.consumer;

import com.fooddelivery.order_service.events.PaymentEvent;
import com.fooddelivery.order_service.order.OrderStatus;
import com.fooddelivery.order_service.service.OrderService;
import com.fooddelivery.order_service.order.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentConsumer {
    private final OrderService orderService;

    @KafkaListener(
            topics = "payment-events",
            groupId = "order-service",
            containerFactory = "paymentKafkaListenerContainerFactory"
    )
    public void handlePaymentEvent(PaymentEvent event){
        log.info("Received Payment Event: {} with payment status {}", event, event.paymentStatus());
        try{
            if ("SUCCESS".equals(event.paymentStatus())) {
                orderService.updateOrderStatus(
                        event.orderId(),
                        PaymentStatus.PAID,
                        OrderStatus.CONFIRMED
                );
            } else if ("FAILED".equals(event.paymentStatus())) {
                orderService.updateOrderStatus(
                        event.orderId(),
                        PaymentStatus.FAILED,
                        OrderStatus.PAYMENT_FAILED
                );
            }
        } catch (Exception ex) {
            log.error(
                    "Ignoring payment event for non-existing order {}",
                    event.orderId(),
                    ex
            );
        }

    }
}
