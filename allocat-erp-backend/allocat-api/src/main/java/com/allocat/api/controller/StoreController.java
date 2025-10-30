package com.allocat.api.controller;

import com.allocat.api.dto.store.CreateStoreRequest;
import com.allocat.api.dto.store.StoreAccessRequest;
import com.allocat.api.dto.store.StoreResponse;
import com.allocat.api.dto.store.UpdateStoreRequest;
import com.allocat.auth.entity.Store;
import com.allocat.auth.service.StoreService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Store Management", description = "APIs for managing stores (SUPER_ADMIN only)")
@SecurityRequirement(name = "Bearer Authentication")
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create a new store", description = "Create a new store (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponse<StoreResponse>> createStore(
            @Valid @RequestBody CreateStoreRequest request) {
        try {
            Store store = Store.builder()
                    .code(request.getCode())
                    .name(request.getName())
                    .accessCode(request.getAccessCode())
                    .address(request.getAddress())
                    .city(request.getCity())
                    .state(request.getState())
                    .country(request.getCountry())
                    .postalCode(request.getPostalCode())
                    .phone(request.getPhone())
                    .email(request.getEmail())
                    .taxId(request.getTaxId())
                    .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                    .timezone(request.getTimezone() != null ? request.getTimezone() : "UTC")
                    .isActive(true)
                    .build();
            
            Store createdStore = storeService.createStore(store);
            
            return ResponseEntity.ok(ApiResponse.success(
                    StoreResponse.fromEntity(createdStore),
                    "Store created successfully"
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error creating store: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error creating store", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error creating store: " + e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get all stores", description = "Retrieve all stores (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponse<List<StoreResponse>>> getAllStores(
            @Parameter(description = "Filter by active status")
            @RequestParam(required = false) Boolean active) {
        try {
            List<Store> stores = active != null && active 
                    ? storeService.getAllActiveStores()
                    : storeService.getAllStores();
            
            List<StoreResponse> storeResponses = stores.stream()
                    .map(StoreResponse::fromEntity)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(
                    storeResponses,
                    "Stores retrieved successfully"
            ));
        } catch (Exception e) {
            log.error("Error retrieving stores", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving stores: " + e.getMessage()));
        }
    }

    @GetMapping("/{storeId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get store by ID", description = "Retrieve store details by ID (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponse<StoreResponse>> getStoreById(
            @Parameter(description = "Store ID") 
            @PathVariable Long storeId) {
        try {
            Store store = storeService.getStoreById(storeId);
            return ResponseEntity.ok(ApiResponse.success(
                    StoreResponse.fromEntity(store),
                    "Store retrieved successfully"
            ));
        } catch (IllegalArgumentException e) {
            log.error("Store not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving store", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving store: " + e.getMessage()));
        }
    }

    @GetMapping("/code/{storeCode}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get store by code", description = "Retrieve store details by code (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponse<StoreResponse>> getStoreByCode(
            @Parameter(description = "Store code") 
            @PathVariable String storeCode) {
        try {
            Store store = storeService.getStoreByCode(storeCode);
            return ResponseEntity.ok(ApiResponse.success(
                    StoreResponse.fromEntity(store),
                    "Store retrieved successfully"
            ));
        } catch (IllegalArgumentException e) {
            log.error("Store not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving store", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving store: " + e.getMessage()));
        }
    }

    @PutMapping("/{storeId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Update store", description = "Update store details (SUPER_ADMIN only, requires access code)")
    public ResponseEntity<ApiResponse<StoreResponse>> updateStore(
            @Parameter(description = "Store ID") 
            @PathVariable Long storeId,
            @Valid @RequestBody UpdateStoreRequest request) {
        try {
            Store updatedData = Store.builder()
                    .name(request.getName())
                    .address(request.getAddress())
                    .city(request.getCity())
                    .state(request.getState())
                    .country(request.getCountry())
                    .postalCode(request.getPostalCode())
                    .phone(request.getPhone())
                    .email(request.getEmail())
                    .taxId(request.getTaxId())
                    .currency(request.getCurrency())
                    .timezone(request.getTimezone())
                    .isActive(request.getIsActive())
                    .build();
            
            Store updatedStore = storeService.updateStore(storeId, updatedData, request.getAccessCode());
            
            return ResponseEntity.ok(ApiResponse.success(
                    StoreResponse.fromEntity(updatedStore),
                    "Store updated successfully"
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error updating store: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error updating store", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error updating store: " + e.getMessage()));
        }
    }

    @PostMapping("/validate-access")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Validate store access code", 
               description = "Validate access code for a store (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponse<Boolean>> validateAccessCode(
            @Valid @RequestBody StoreAccessRequest request) {
        try {
            boolean isValid;
            
            if (request.getStoreId() != null) {
                isValid = storeService.validateAccessCode(request.getStoreId(), request.getAccessCode());
            } else if (request.getStoreCode() != null) {
                isValid = storeService.validateAccessCodeByCode(request.getStoreCode(), request.getAccessCode());
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Either storeId or storeCode must be provided"));
            }
            
            if (isValid) {
                return ResponseEntity.ok(ApiResponse.success(true, "Access code is valid"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid access code"));
            }
        } catch (IllegalArgumentException e) {
            log.error("Store not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error validating access code", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error validating access code: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{storeId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete store", description = "Soft delete a store (SUPER_ADMIN only, requires access code)")
    public ResponseEntity<ApiResponse<Void>> deleteStore(
            @Parameter(description = "Store ID") 
            @PathVariable Long storeId,
            @Valid @RequestBody StoreAccessRequest request) {
        try {
            storeService.deleteStore(storeId, request.getAccessCode());
            return ResponseEntity.ok(ApiResponse.success(null, "Store deleted successfully"));
        } catch (IllegalArgumentException e) {
            log.error("Error deleting store: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error deleting store", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting store: " + e.getMessage()));
        }
    }
}

