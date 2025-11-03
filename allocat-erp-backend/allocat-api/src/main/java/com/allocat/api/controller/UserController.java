package com.allocat.api.controller;

import com.allocat.api.dto.user.UserResponse;
import com.allocat.auth.entity.Role;
import com.allocat.auth.entity.Store;
import com.allocat.auth.entity.User;
import com.allocat.auth.service.StoreService;
import com.allocat.auth.service.UserService;
import com.allocat.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final StoreService storeService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieve a list of all users in the system (SUPER_ADMIN or ADMIN)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved users list"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = auth.getName();
            User currentUser = userService.getUserByUsername(currentUsername);
            
            List<User> users;
            // If ADMIN, only show users from the same store
            if (currentUser.getRole().getName().equals("ADMIN") && currentUser.getStoreId() != null) {
                users = userService.getUsersByStoreId(currentUser.getStoreId());
            } else {
                // SUPER_ADMIN sees all users
                users = userService.getAllUsers();
            }
            
            // Convert to UserResponse with store codes
            List<UserResponse> userResponses = users.stream()
                    .map(user -> {
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
                        return UserResponse.fromEntity(user, storeCode, storeName);
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(userResponses, "Users retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving users: " + e.getMessage()));
        }
    }
    
    @GetMapping("/roles")
    @Operation(summary = "Get all roles", description = "Retrieve a list of all available roles with their permissions")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved roles list"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {
        List<Role> roles = userService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }
    
    public record CreateUserRequest(
            @Schema(example = "john_doe") String username,
            @Schema(example = "john@example.com") String email,
            String password,
            @Schema(example = "John") String firstName,
            @Schema(example = "Doe") String lastName,
            @Schema(example = "+1234567890") String phone,
            @Schema(example = "SALES_STAFF", description = "Role name (e.g., SUPER_ADMIN, ADMIN, STORE_MANAGER, etc.)") String roleName,
            @Schema(example = "STR001", description = "Store Code (alphanumeric like STR001, CRK2645, etc.)") String storeCode,
            @Schema(description = "Store ID (deprecated - use storeCode instead)", deprecated = true) Long storeId
    ) {}
    
    @PostMapping
    @Operation(summary = "Create a new user", description = "Create a new user in the system")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid user data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Parameter(description = "User object to be created", required = true)
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User details",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreateUserRequest.class),
                            examples = @ExampleObject(
                                    name = "Example User",
                                    value = """
                                            {
                                                "username": "john_doe",
                                                "email": "john@example.com",
                                                "password": "password",
                                                "firstName": "John",
                                                "lastName": "Doe",
                                                "phone": "+1234567890"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody CreateUserRequest req) {
        try {
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = auth.getName();
            User currentUser = userService.getUserByUsername(currentUsername);
            String currentUserRole = currentUser.getRole().getName();
            
            // Set role (default to VIEWER if not specified)
            String roleName = (req.roleName() == null || req.roleName().isBlank()) ? "VIEWER" : req.roleName();
            
            // Determine store ID from store code or storeId (backward compatibility)
            Long assignedStoreId = null;
            String assignedStoreCode = null;
            String assignedStoreName = null;
            
            // Prefer storeCode over storeId
            if (req.storeCode() != null && !req.storeCode().isBlank()) {
                // Check if store exists by code
                if (!storeService.existsByCode(req.storeCode())) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("Store with code '" + req.storeCode() + "' does not exist. Please use a valid store code (e.g., STR001, CRK2645)."));
                }
                
                // Get the store by code
                try {
                    Store store = storeService.getStoreByCode(req.storeCode());
                    assignedStoreId = store.getId();
                    assignedStoreCode = store.getCode();
                    assignedStoreName = store.getName();
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("Store with code '" + req.storeCode() + "' not found."));
                }
            } else if (req.storeId() != null) {
                // Backward compatibility: accept storeId
                try {
                    Store store = storeService.getStoreById(req.storeId());
                    assignedStoreId = store.getId();
                    assignedStoreCode = store.getCode();
                    assignedStoreName = store.getName();
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("Store with ID '" + req.storeId() + "' not found. Please use storeCode instead."));
                }
            }
            
            // Validate role assignment permissions
            if ("ADMIN".equals(roleName)) {
                // Only SUPER_ADMIN can create ADMIN users
                if (!"SUPER_ADMIN".equals(currentUserRole)) {
                    log.warn("User {} attempted to create ADMIN user without SUPER_ADMIN role", currentUsername);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.error("Only SUPER_ADMIN can create ADMIN users"));
                }
                
                // ADMIN role must have a store assigned
                if (assignedStoreId == null) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("ADMIN role requires a store assignment. Please provide a valid storeCode."));
                }
            }
            
            if ("SUPER_ADMIN".equals(roleName)) {
                // No one can create SUPER_ADMIN users
                log.warn("Attempt to create SUPER_ADMIN user blocked");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("SUPER_ADMIN users cannot be created through this endpoint"));
            }
            
            // If current user is ADMIN, enforce store restriction
            if ("ADMIN".equals(currentUserRole)) {
                // ADMIN can only create users for their own store
                if (assignedStoreId == null || !assignedStoreId.equals(currentUser.getStoreId())) {
                    log.warn("ADMIN {} attempted to create user for different store", currentUsername);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.error("ADMIN can only create users for their assigned store"));
                }
                assignedStoreId = currentUser.getStoreId();
            }
            
            String rawPassword = (req.password() == null || req.password().isBlank()) ? generateTempPassword() : req.password();
            
            User toCreate = User.builder()
                    .username(req.username())
                    .email(req.email())
                    .passwordHash(passwordEncoder.encode(rawPassword))
                    .firstName(req.firstName())
                    .lastName(req.lastName())
                    .phone(req.phone())
                    .role(userService.findRoleByName(roleName))
                    .storeId(assignedStoreId)
                    .build();
            
            User createdUser = userService.createUser(toCreate);
            log.info("User created successfully: {} by {}", createdUser.getUsername(), currentUsername);
            
            // Return UserResponse with store code
            UserResponse response = UserResponse.fromEntity(createdUser, assignedStoreCode, assignedStoreName);
            return ResponseEntity.ok(ApiResponse.success(response, "User created successfully"));
            
        } catch (IllegalArgumentException e) {
            log.error("Error creating user: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error creating user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error creating user: " + e.getMessage()));
        }
    }
    
    private String generateTempPassword() {
        byte[] bytes = new byte[12];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
