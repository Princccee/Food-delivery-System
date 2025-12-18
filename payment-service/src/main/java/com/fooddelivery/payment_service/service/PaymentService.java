package com.fooddelivery.payment_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.payment_service.dto.InitiateRequest;
import com.fooddelivery.payment_service.dto.InitiateResponse;
import com.fooddelivery.payment_service.dto.VerifyRequest;
import com.fooddelivery.payment_service.model.Payment;
import com.fooddelivery.payment_service.model.PaymentStatus;
import com.fooddelivery.payment_service.repository.PaymentRepository;
import com.fooddelivery.payment_service.util.HmacUtils;
import com.fooddelivery.events.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    @Value("${order.service.base-url}")
    private String orderServiceBaseUrl;

    @Value("${razorpay.webhook-secret}")
    private String razorpayWebhookSecret;

    private static final String RAZORPAY_ORDER_API = "https://api.razorpay.com/v1/orders";

    @Transactional
    public InitiateResponse initiateOrder(InitiateRequest req) {
        // create local payment entry
        Payment payment = Payment.builder()
                .orderId(req.getOrderId())
                .amount(req.getAmount())
                .currency(req.getCurrency() == null ? "INR" : req.getCurrency())
                .status(PaymentStatus.PENDING)
                .createdAt(Instant.now())
//                .rawPayload(null)
                .build();

        payment = paymentRepository.save(payment);

        // create Razorpay order
        Map<String, Object> payload = Map.of(
                "amount", req.getAmount(),
                "currency", payment.getCurrency(),
                "receipt", req.getOrderId().toString(),
                "payment_capture", 1
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(razorpayKeyId, razorpayKeySecret);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<JsonNode> response = restTemplate.exchange(RAZORPAY_ORDER_API, HttpMethod.POST, entity, JsonNode.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to create razorpay order");
        }

        String razorpayOrderId = response.getBody().get("id").asText();
        payment.setRazorpayOrderId(razorpayOrderId);
        paymentRepository.save(payment);

        return new InitiateResponse(payment.getId(), razorpayOrderId, razorpayKeyId, payment.getAmount(), payment.getCurrency());
    }

    @Transactional
    public void verifyClientPayment(VerifyRequest req) {
        // validate signature: order_id|payment_id
        String payload = req.getRazorpayOrderId() + "|" + req.getRazorpayPaymentId();
        String signature = HmacUtils.hmacSha256Hex(razorpayKeySecret, payload);
        if (!signature.equals(req.getRazorpaySignature())) {
            throw new RuntimeException("Invalid signature");
        }

        Payment p = paymentRepository.findById(req.getPaymentId())
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        p.setRazorpayPaymentId(req.getRazorpayPaymentId());
        p.setStatus(PaymentStatus.SUCCESS);
        p.setUpdatedAt(Instant.now());
        paymentRepository.save(p);

        notifyOrderService(p, "SUCCESS");
    }

    @Transactional
    public void handleWebhook(String rawBody, String signatureHeader) {

        // compute sha256 HMAC using webhook secret (NOT API secret)
        String computedSignature = HmacUtils.hmacSha256Hex(razorpayWebhookSecret, rawBody);

        if (!computedSignature.equals(signatureHeader)) {
            throw new RuntimeException("Invalid webhook signature");
        }

        try {
            JsonNode event = objectMapper.readTree(rawBody);
            String eventName = event.path("event").asText();

            if ("payment.captured".equals(eventName) || "payment.authorized".equals(eventName)) {

                JsonNode entity = event.at("/payload/payment/entity");

                String rOrderId = entity.get("order_id").asText();
                String rPaymentId = entity.get("id").asText();

                Optional<Payment> opt = paymentRepository.findByRazorpayOrderId(rOrderId);
                if (opt.isEmpty()) return;

                Payment p = opt.get();
                if (p.getStatus() == PaymentStatus.SUCCESS) return; // idempotency

                p.setRazorpayPaymentId(rPaymentId);
                p.setStatus(PaymentStatus.SUCCESS);
                p.setUpdatedAt(Instant.now());
//                if (p.getRawPayload() == null) {
//                    p.setRawPayload(rawBody);
//                }

                paymentRepository.save(p);

                notifyOrderService(p, "SUCCESS");

            } else if ("payment.failed".equals(eventName)) {

                JsonNode entity = event.at("/payload/payment/entity");
                String rOrderId = entity.get("order_id").asText();

                Optional<Payment> opt = paymentRepository.findByRazorpayOrderId(rOrderId);
                if (opt.isPresent()) {
                    Payment p = opt.get();
                    p.setStatus(PaymentStatus.FAILED);
                    p.setUpdatedAt(Instant.now());
//                    if (p.getRawPayload() == null) {
//                        p.setRawPayload(rawBody);
//                    }
                    paymentRepository.save(p);

                    notifyOrderService(p, "FAILED");
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to process webhook", e);
        }
    }


    private void notifyOrderService(Payment p, String status) {
        // call Order Service internal callback endpoint
        String url = orderServiceBaseUrl + "/api/orders/" + p.getOrderId() + "/payment-callback";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // OPTIONAL: add an internal shared secret header here for safety
            Map<String, String> body = Map.of(
                    "paymentId", p.getId().toString(),
                    "razorpayPaymentId", p.getRazorpayPaymentId() == null ? "" : p.getRazorpayPaymentId(),
                    "status", status
            );
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, entity, Void.class);
        } catch (Exception ex) {
            // log and retry strategy could be added
            throw new RuntimeException("Failed to notify Order Service", ex);
        }
    }

    public Optional<Payment> findById(UUID id) {
        return paymentRepository.findById(id);
    }

    @Transactional
    public void createPaymentFromOrder(OrderCreatedEvent event) {

        // Idempotency: avoid duplicate payment rows
        if (paymentRepository.findByOrderId(event.getOrderId()).isPresent()) {
            log.info("⚠️ Payment already exists for order {}", event.getOrderId());
            return;
        }

        System.out.println("Amount: " + event.getAmount());
        Payment payment = Payment.builder()
                .orderId(event.getOrderId())
                .amount((event.getAmount() * 100)) // paise
                .currency("INR")
                .status(PaymentStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        paymentRepository.save(payment);

        log.info("💰 Payment record CREATED for order {}", event.getOrderId());
    }
}
