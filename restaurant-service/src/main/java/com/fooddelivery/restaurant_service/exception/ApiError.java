package com.fooddelivery.restaurant_service.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ApiError {
    private String message;
    private LocalDateTime timestamp;
}
