package com.allocat.api.controller;

import com.allocat.api.dto.useraccess.*;
import com.allocat.auth.entity.UserStoreAccess;
import com.allocat.auth.repository.UserStoreAccessRepository;
import com.allocat.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.allocat.auth.util.SecurityUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users/{userId}/store-access")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Store Access Management", description = "APIs for managing user access to stores and warehouses")
@SecurityRequirement(name = "Bearer Authentication")
public class UserStoreAccessController {

    private final UserStoreAccessRepository userStoreAccessRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Grant store access to user", description = "Grant a user access to a store or warehouse")
    public ResponseEntity<ApiResponse<UserStoreAccessResponse>> grantAccess(
            @Parameter(description = "User ID")
            @PathVariable Long userId,
            @Valid @RequestBody CreateUserStoreAccessRequest request) {
        try {
            // Get current user ID from security context
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long grantedById = getCurrentUserId(auth);

            UserStoreAccess access = UserStoreAccess.builder()
                    .user(com.allocat.auth.entity.User.builder().id(userId).build())
                    .store(com.allocat.auth.entity.Store.builder().id(request.getStoreId()).build())
                    .warehouse(request.getWarehouseId() != null ? 
                              com.allocat.inventory.entity.Warehouse.builder().id(request.getWarehouseId()).build() : null)
                    .accessLevel(request.getAccessLevel() != null ? 
                                UserStoreAccess.AccessLevel.valueOf(request.getAccessLevel()) : 
                                UserStoreAccess.AccessLevel.VIEW)
                    .isPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false)
                    .grantedBy(grantedById != null ? 
                              com.allocat.auth.entity.User.builder().id(grantedById).build() : null)
                    .grantedAt(LocalDateTime.now())
                    .expiresAt(request.getExpiresAt())
                    .isActive(true)
                    .notes(request.getNotes())
                    .build();

            UserStoreAccess saved = userStoreAccessRepository.save(access);
            return ResponseEntity.ok(ApiResponse.success(
                    UserStoreAccessResponse.fromEntity(saved),
                    "Store access granted successfully"
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error granting store access: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error granting store access", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error granting store access: " + e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Get user store access", description = "Retrieve all store access for a user")
    public ResponseEntity<ApiResponse<List<UserStoreAccessResponse>>> getUserAccess(
            @Parameter(description = "User ID")
            @PathVariable Long userId,
            @Parameter(description = "Filter by active status")
            @RequestParam(required = false) Boolean active) {
        try {
            List<UserStoreAccess> accesses;
            if (active != null && active) {
                accesses = userStoreAccessRepository.findByUserIdAndIsActive(userId, true);
            } else {
                accesses = userStoreAccessRepository.findByUserId(userId);
            }

            List<UserStoreAccessResponse> responses = accesses.stream()
                    .map(UserStoreAccessResponse::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(
                    responses,
                    "User store access retrieved successfully"
            ));
        } catch (Exception e) {
            log.error("Error retrieving user store access", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving user store access: " + e.getMessage()));
        }
    }

    @PutMapping("/{accessId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Update user store access", description = "Update user access to a store or warehouse")
    public ResponseEntity<ApiResponse<UserStoreAccessResponse>> updateAccess(
            @Parameter(description = "User ID")
            @PathVariable Long userId,
            @Parameter(description = "Access ID")
            @PathVariable Long accessId,
            @Valid @RequestBody UpdateUserStoreAccessRequest request) {
        try {
            UserStoreAccess access = userStoreAccessRepository.findById(accessId)
                    .orElseThrow(() -> new RuntimeException("Access not found: " + accessId));

            if (!access.getUser().getId().equals(userId)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Access does not belong to this user"));
            }

            if (request.getAccessLevel() != null) {
                access.setAccessLevel(UserStoreAccess.AccessLevel.valueOf(request.getAccessLevel()));
            }
            if (request.getIsPrimary() != null) {
                access.setIsPrimary(request.getIsPrimary());
            }
            if (request.getIsActive() != null) {
                access.setIsActive(request.getIsActive());
            }
            if (request.getExpiresAt() != null) {
                access.setExpiresAt(request.getExpiresAt());
            }
            if (request.getNotes() != null) {
                access.setNotes(request.getNotes());
            }

            UserStoreAccess updated = userStoreAccessRepository.save(access);
            return ResponseEntity.ok(ApiResponse.success(
                    UserStoreAccessResponse.fromEntity(updated),
                    "Store access updated successfully"
            ));
        } catch (RuntimeException e) {
            log.error("Error updating store access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error updating store access", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error updating store access: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{accessId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Revoke user store access", description = "Revoke user access to a store or warehouse")
    public ResponseEntity<ApiResponse<Void>> revokeAccess(
            @Parameter(description = "User ID")
            @PathVariable Long userId,
            @Parameter(description = "Access ID")
            @PathVariable Long accessId) {
        try {
            UserStoreAccess access = userStoreAccessRepository.findById(accessId)
                    .orElseThrow(() -> new RuntimeException("Access not found: " + accessId));

            if (!access.getUser().getId().equals(userId)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Access does not belong to this user"));
            }

            access.setIsActive(false);
            userStoreAccessRepository.save(access);

            return ResponseEntity.ok(ApiResponse.success(null, "Store access revoked successfully"));
        } catch (RuntimeException e) {
            log.error("Error revoking store access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error revoking store access", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error revoking store access: " + e.getMessage()));
        }
    }

    // Helper method to extract user ID from authentication
    private Long getCurrentUserId(Authentication auth) {
        // SecurityUtils reads from the SecurityContext; keep the signature to avoid touching call sites.
        return SecurityUtils.getCurrentUserId();
    }
}
