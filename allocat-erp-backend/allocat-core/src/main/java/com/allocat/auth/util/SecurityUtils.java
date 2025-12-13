package com.allocat.auth.util;

import com.allocat.security.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Utility class for extracting user information from security context
 */
@Component
@Slf4j
public class SecurityUtils {

    private static JwtUtil jwtUtil;

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        SecurityUtils.jwtUtil = jwtUtil;
    }

    /**
     * Get current user ID from security context
     */
    public static Long getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                return null;
            }

            // Try to extract from JWT token if available
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    try {
                        Long userId = jwtUtil.extractUserId(token);
                        if (userId != null) {
                            return userId;
                        }
                    } catch (Exception e) {
                        log.debug("Could not extract user ID from token: {}", e.getMessage());
                    }
                }
            }

            // Fallback: try to extract from principal
            Object principal = auth.getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                // If you have a custom UserDetails implementation with userId
                // Extract it here
            }

            // Last resort: extract from authentication name/principal
            String username = auth.getName();
            if (username != null && !username.equals("anonymousUser")) {
                // You might need to look up user by username to get ID
                // For now, return null and let the caller handle it
                log.warn("Could not extract user ID from authentication. Username: {}", username);
            }

            return null;
        } catch (Exception e) {
            log.error("Error extracting current user ID", e);
            return null;
        }
    }

    /**
     * Get current username from security context
     */
    public static String getCurrentUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                return null;
            }
            return auth.getName();
        } catch (Exception e) {
            log.error("Error extracting current username", e);
            return null;
        }
    }

    /**
     * Get current user ID from authentication object
     */
    public static Long getCurrentUserId(Authentication auth) {
        if (auth == null) {
            return null;
        }

        // Try to extract from JWT token
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    return jwtUtil.extractUserId(token);
                } catch (Exception e) {
                    log.debug("Could not extract user ID from token: {}", e.getMessage());
                }
            }
        }

        return null;
    }

    /**
     * Check if current user is authenticated
     */
    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && 
               !auth.getName().equals("anonymousUser");
    }

    /**
     * Check if current user has a specific role
     */
    public static boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role) || 
                             a.getAuthority().equals(role));
    }

    /**
     * Get current HTTP request
     */
    private static HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
