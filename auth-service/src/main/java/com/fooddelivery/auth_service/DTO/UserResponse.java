package com.fooddelivery.auth_service.DTO;

import com.fooddelivery.auth_service.user.Role;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private Role role;
}
