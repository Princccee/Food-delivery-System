package com.fooddelivery.auth_service.DTO;

import com.fooddelivery.auth_service.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String name;

    @Email
    private String email;

    @NotBlank
    private String password;

    private String phone;

    // optional: default CUSTOMER if null
    private Role role;
}
