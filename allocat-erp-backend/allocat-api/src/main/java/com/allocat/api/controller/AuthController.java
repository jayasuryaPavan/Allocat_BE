package com.allocat.api.controller;

import com.allocat.api.dto.AuthResponse;
import com.allocat.api.dto.LoginRequest;
import com.allocat.auth.entity.Store;
import com.allocat.auth.entity.User;
import com.allocat.auth.service.AuthService;
import com.allocat.auth.service.StoreService;
import com.allocat.common.dto.ApiResponse;
import com.allocat.security.jwt.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
@Slf4j
public class AuthController {
    
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final StoreService storeService;
    
    @Value("${jwt.expiration}")
    private Long jwtExpiration;
    
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT tokens")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "Login successful",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", 
                    description = "Invalid credentials"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", 
                    description = "Bad request"
            )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    name = "Login Example",
                                    value = """
                                            {
                                                "username": "surya_example",
                                                "password": "password_example"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody LoginRequest loginRequest) {
        
        try {
            // Authenticate user
            User user = authService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());
            
            // Generate tokens
            String roleName = user.getRole() != null ? user.getRole().getName() : "VIEWER";
            Long roleId = user.getRole() != null ? user.getRole().getId() : null;
            String accessToken = jwtUtil.generateToken(user.getUsername(), roleName, user.getId());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
            
            // Get store details if user has a store
            String storeCode = null;
            String storeName = null;
            if (user.getStoreId() != null) {
                try {
                    Store store = storeService.getStoreById(user.getStoreId());
                    storeCode = store.getCode();
                    storeName = store.getName();
                } catch (Exception e) {
                    log.warn("Store not found for user {}: {}", user.getUsername(), e.getMessage());
                }
            }
            
            // Build response with all user details
            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtExpiration)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .phone(user.getPhone())
                    .roleId(roleId)
                    .role(roleName)
                    .storeId(user.getStoreId())
                    .storeCode(storeCode)
                    .storeName(storeName)
                    .isActive(user.getActive())
                    .lastLoginAt(user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : null)
                    .permissions(user.getRole() != null ? user.getRole().getPermissions() : new String[0])
                    .build();
            
            log.info("Login successful for user: {}", user.getUsername());
            return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful"));
            
        } catch (IllegalArgumentException e) {
            log.error("Login failed: {}", e.getMessage());
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get the currently authenticated user's details")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "User details retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", 
                    description = "Unauthorized - Invalid or expired token"
            )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            // Extract token from Bearer header
            String token = authHeader.startsWith("Bearer ") ? 
                    authHeader.substring(7) : authHeader;
            
            // Extract username from token
            String username = jwtUtil.extractUsername(token);
            
            // Validate token
            if (!jwtUtil.validateToken(token, username)) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Invalid or expired token"));
            }
            
            // Get user from database
            User user = authService.getUserByUsername(username);
            
            // Get store details if user has a store
            String storeCode = null;
            String storeName = null;
            if (user.getStoreId() != null) {
                try {
                    Store store = storeService.getStoreById(user.getStoreId());
                    storeCode = store.getCode();
                    storeName = store.getName();
                } catch (Exception e) {
                    log.warn("Store not found for user {}: {}", user.getUsername(), e.getMessage());
                }
            }
            
            // Build response with all user details
            String roleName = user.getRole() != null ? user.getRole().getName() : "VIEWER";
            Long roleId = user.getRole() != null ? user.getRole().getId() : null;
            
            AuthResponse authResponse = AuthResponse.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .phone(user.getPhone())
                    .roleId(roleId)
                    .role(roleName)
                    .storeId(user.getStoreId())
                    .storeCode(storeCode)
                    .storeName(storeName)
                    .isActive(user.getActive())
                    .lastLoginAt(user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : null)
                    .permissions(user.getRole() != null ? user.getRole().getPermissions() : new String[0])
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success(authResponse, "User details retrieved successfully"));
            
        } catch (Exception e) {
            log.error("Failed to get current user: {}", e.getMessage());
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Failed to retrieve user details"));
        }
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Get a new access token using refresh token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "Token refreshed successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", 
                    description = "Invalid or expired refresh token"
            )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestHeader("Authorization") String refreshToken) {
        
        try {
            // Remove "Bearer " prefix if present
            String token = refreshToken.startsWith("Bearer ") ? 
                    refreshToken.substring(7) : refreshToken;
            
            // Extract username and validate token
            String username = jwtUtil.extractUsername(token);
            
            if (!jwtUtil.validateToken(token, username)) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Invalid or expired refresh token"));
            }
            
            // Get user details (you might want to create a method in AuthService for this)
            // For now, we'll generate a new token with basic info
            String newAccessToken = jwtUtil.generateToken(username, "VIEWER", null);
            
            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(token)
                    .tokenType("Bearer")
                    .expiresIn(jwtExpiration)
                    .username(username)
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success(authResponse, "Token refreshed successfully"));
            
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Token refresh failed"));
        }
    }
    
    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user and invalidate tokens")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "Logout successful"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", 
                    description = "Invalid or expired token"
            )
    })
    public ResponseEntity<ApiResponse<Object>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtUtil.extractUsername(token);
                
                // TODO: Optionally invalidate refresh token from database
                // authService.invalidateRefreshToken(username);
                
                log.info("User logged out: {}", username);
            }
            
            return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
            
        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            // Even if logout fails, return success (client should clear tokens anyway)
            return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
        }
    }
}

