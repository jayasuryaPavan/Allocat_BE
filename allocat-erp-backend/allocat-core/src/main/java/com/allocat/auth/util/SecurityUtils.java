package com.allocat.auth.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility class for extracting user information from security context.
 * This version does not depend on JwtUtil to avoid circular dependencies.
 */
@Component
@Slf4j
public class SecurityUtils {

    /**
     * Get current user ID from security context.
     * The user ID is expected to be stored in the authentication details or credentials.
     */
    public static Long getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return null;
            }

            // Try to get from authentication details (set by JWT filter)
            Object details = auth.getDetails();
            if (details instanceof Long) {
                return (Long) details;
            }
            
            // Try to parse from principal if it's a numeric string
            Object principal = auth.getPrincipal();
            if (principal instanceof String) {
                try {
                    return Long.parseLong((String) principal);
                } catch (NumberFormatException e) {
                    // Principal is not a numeric ID
                }
            }

            // Try credentials (some implementations store user ID here)
            Object credentials = auth.getCredentials();
            if (credentials instanceof Long) {
                return (Long) credentials;
            }

            log.debug("Could not extract user ID from authentication. Principal: {}", auth.getName());
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
     * Check if current user has any of the specified roles
     */
    public static boolean hasAnyRole(String... roles) {
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if current user is an admin (SUPER_ADMIN or ADMIN)
     */
    public static boolean isAdmin() {
        return hasAnyRole("SUPER_ADMIN", "ADMIN");
    }
}
