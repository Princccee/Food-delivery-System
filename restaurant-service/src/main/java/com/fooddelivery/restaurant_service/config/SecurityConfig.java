package com.fooddelivery.restaurant_service.config;

import com.fooddelivery.restaurant_service.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // public endpoints
                        .requestMatchers("/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/restaurants/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/menu-items/**").permitAll()
                        // owner-only endpoints
                        .requestMatchers(HttpMethod.POST, "/restaurants/**").hasRole("RESTAURANT_OWNER")
                        .requestMatchers(HttpMethod.PUT, "/restaurants/**").hasRole("RESTAURANT_OWNER")
                        .requestMatchers(HttpMethod.DELETE, "/restaurants/**").hasRole("RESTAURANT_OWNER")
//                        .requestMatchers("/menu-items/**").hasRole("RESTAURANT_OWNER")
                        // any other must be authenticated
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
