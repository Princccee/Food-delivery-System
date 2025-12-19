package com.fooddelivery.payment_service.kafka;

import com.fooddelivery.payment_service.events.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentEventProducer {
    private static final String PAYMENT_EVENT_TOPIC = "payment-events";

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public void publishPaymentEvent(PaymentEvent event){
        kafkaTemplate.send(
                PAYMENT_EVENT_TOPIC,
                event.orderId().toString(),
                event
        );
    }
}
