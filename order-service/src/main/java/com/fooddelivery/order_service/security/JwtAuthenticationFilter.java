package com.fooddelivery.order_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String path = request.getRequestURI();
        final String method = request.getMethod();
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        try {
            if (jwtService.isTokenExpired(token)) {
                System.out.println("DEBUG: Token is expired for path: " + path);
                filterChain.doFilter(request, response);
                return;
            }

            String username = jwtService.extractUsername(token);
            String role = jwtService.extractRole(token);
            String userId = jwtService.extractUserId(token);

            System.out.println("DEBUG: Authenticating User: " + username + " with Role: " + role + " for Path: " + path + " Method: " + method);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var auth = new UsernamePasswordAuthenticationToken(
                        username,
                        userId,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
                System.out.println("DEBUG: Authentication set in SecurityContext for: " + username);
            }
        } catch (Exception e) {
            System.err.println("DEBUG: JWT Validation failed for path " + path + ": " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
