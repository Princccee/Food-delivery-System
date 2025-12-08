package com.fooddelivery.payment_service.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/health")
    public String Health(){
        return "Payment Service is up";
    }
}
