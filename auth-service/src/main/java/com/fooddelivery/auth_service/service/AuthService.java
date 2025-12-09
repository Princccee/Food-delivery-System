package com.fooddelivery.auth_service.service;

import com.fooddelivery.auth_service.DTO.AuthResponse;
import com.fooddelivery.auth_service.DTO.LoginRequest;
import com.fooddelivery.auth_service.DTO.RegisterRequest;
import com.fooddelivery.auth_service.security.JwtService;
import com.fooddelivery.auth_service.user.Role;
import com.fooddelivery.auth_service.user.User;
import com.fooddelivery.auth_service.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        Role role = request.getRole() != null ? request.getRole() : Role.CUSTOMER;

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(role)
                .build();

        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        // authenticate with email/password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // if no exception -> valid credentials
        String jwt = jwtService.generateToken(request.getEmail());
        return new AuthResponse(jwt);
    }
}
