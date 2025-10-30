package com.allocat.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        log.info("Processing request: {} {}", request.getMethod(), request.getRequestURI());
        log.info("Authorization header: {}", authHeader != null ? "Bearer ***" : "null");

        // Check if Authorization header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("No valid Authorization header found for: {} {}", request.getMethod(), request.getRequestURI());
            // Mark this request as potentially a 404 so the entry point can handle it better
            request.setAttribute("missing-auth-header", true);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract JWT token
            jwt = authHeader.substring(7);
            log.info("Extracted JWT token (first 20 chars): {}...", jwt.length() > 20 ? jwt.substring(0, 20) : jwt);
            
            username = jwtUtil.extractUsername(jwt);
            log.info("Extracted username from token: {}", username);

            // If username is valid and user is not already authenticated
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Validate token
                if (jwtUtil.validateToken(jwt, username)) {
                    // Extract role from token
                    String role = jwtUtil.extractRole(jwt);
                    log.info("Token validated successfully. User: {}, Role: {}", username, role);
                    
                    // Create authentication token with role as authority
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                    );
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    log.info("Successfully authenticated user: {} with role: {}", username, role);
                } else {
                    log.warn("Token validation failed for user: {}", username);
                }
            } else if (username == null) {
                log.warn("Username extracted from token is null");
            } else {
                log.info("User already authenticated: {}", SecurityContextHolder.getContext().getAuthentication().getName());
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {} - {}", e.getMessage(), e.getClass().getName());
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }
}

