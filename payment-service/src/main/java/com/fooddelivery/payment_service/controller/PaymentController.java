package com.fooddelivery.payment_service.controller;

import com.fooddelivery.payment_service.dto.InitiateResponse;
import com.fooddelivery.payment_service.dto.VerifyRequest;
import com.fooddelivery.payment_service.model.Payment;
import com.fooddelivery.payment_service.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate/{orderId}")
    public ResponseEntity<InitiateResponse> makePayment(@Valid @PathVariable UUID orderId) {
        InitiateResponse resp = paymentService.makePayment(orderId);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@Valid @RequestBody VerifyRequest req) {
        paymentService.verifyClientPayment(req);
        return ResponseEntity.ok().body(Map.of("status", "ok"));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            HttpServletRequest request,
            @RequestHeader("X-Razorpay-Signature") String signature
    ) throws IOException {

        byte[] rawBytes = request.getInputStream().readAllBytes();
        String rawBody = new String(rawBytes, StandardCharsets.UTF_8);

        paymentService.handleWebhook(rawBody, signature);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPayment(@PathVariable UUID id) {
        return paymentService.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
