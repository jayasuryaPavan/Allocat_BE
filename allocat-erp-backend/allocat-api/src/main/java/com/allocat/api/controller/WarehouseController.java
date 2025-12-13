package com.allocat.api.controller;

import com.allocat.api.dto.warehouse.*;
import com.allocat.common.dto.ApiResponse;
import com.allocat.inventory.entity.Warehouse;
import com.allocat.inventory.entity.WarehouseLocation;
import com.allocat.inventory.service.WarehouseService;
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
import com.allocat.auth.entity.UserStoreAccess;
import com.allocat.auth.service.AccessControlService;
import com.allocat.auth.util.SecurityUtils;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Warehouse Management", description = "APIs for managing warehouses and locations")
@SecurityRequirement(name = "Bearer Authentication")
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final AccessControlService accessControlService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Create a new warehouse", description = "Create a new warehouse for a store")
    public ResponseEntity<ApiResponse<WarehouseResponse>> createWarehouse(
            @Valid @RequestBody CreateWarehouseRequest request) {
        try {
            // Verify user has access to the store
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId != null) {
                accessControlService.verifyStoreAccess(
                    userId, 
                    request.getStoreId(), 
                    UserStoreAccess.AccessLevel.MANAGE
                );
            }
            Warehouse warehouse = Warehouse.builder()
                    .code(request.getCode())
                    .name(request.getName())
                    .store(com.allocat.auth.entity.Store.builder().id(request.getStoreId()).build())
                    .type(request.getType() != null ? 
                          Warehouse.WarehouseType.valueOf(request.getType()) : 
                          Warehouse.WarehouseType.WAREHOUSE)
                    .address(request.getAddress())
                    .city(request.getCity())
                    .state(request.getState())
                    .country(request.getCountry())
                    .postalCode(request.getPostalCode())
                    .phone(request.getPhone())
                    .email(request.getEmail())
                    .manager(request.getManagerId() != null ? 
                            com.allocat.auth.entity.User.builder().id(request.getManagerId()).build() : null)
                    .settings(request.getSettings())
                    .isActive(true)
                    .build();

            Warehouse created = warehouseService.createWarehouse(warehouse);
            return ResponseEntity.ok(ApiResponse.success(
                    WarehouseResponse.fromEntity(created),
                    "Warehouse created successfully"
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error creating warehouse: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error creating warehouse", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error creating warehouse: " + e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'OPERATOR')")
    @Operation(summary = "Get all warehouses", description = "Retrieve all warehouses, optionally filtered by store")
    public ResponseEntity<ApiResponse<List<WarehouseResponse>>> getAllWarehouses(
            @Parameter(description = "Filter by store ID")
            @RequestParam(required = false) Long storeId,
            @Parameter(description = "Filter by active status")
            @RequestParam(required = false) Boolean active) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            List<Warehouse> warehouses;
            
            if (storeId != null) {
                // Verify user has access to this store
                if (userId != null) {
                    accessControlService.verifyStoreAccess(
                        userId, 
                        storeId, 
                        UserStoreAccess.AccessLevel.VIEW
                    );
                }
                warehouses = warehouseService.getWarehousesByStore(storeId);
            } else {
                // Filter by user's accessible stores
                if (userId != null && !SecurityUtils.hasRole("SUPER_ADMIN") && !SecurityUtils.hasRole("ADMIN")) {
                    List<Long> accessibleStoreIds = accessControlService.getAccessibleStoreIds(userId);
                    if (accessibleStoreIds.isEmpty()) {
                        return ResponseEntity.ok(ApiResponse.success(
                                List.of(),
                                "No warehouses accessible"
                        ));
                    }
                    // Get warehouses for accessible stores
                    warehouses = accessibleStoreIds.stream()
                            .flatMap(sid -> warehouseService.getWarehousesByStore(sid).stream())
                            .distinct()
                            .collect(Collectors.toList());
                } else {
                    warehouses = active != null && active ? 
                            warehouseService.getAllActiveWarehouses() : 
                            warehouseService.getAllActiveWarehouses();
                }
            }

            List<WarehouseResponse> responses = warehouses.stream()
                    .map(WarehouseResponse::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(
                    responses,
                    "Warehouses retrieved successfully"
            ));
        } catch (Exception e) {
            log.error("Error retrieving warehouses", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving warehouses: " + e.getMessage()));
        }
    }

    @GetMapping("/{warehouseId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'OPERATOR')")
    @Operation(summary = "Get warehouse by ID", description = "Retrieve warehouse details by ID")
    public ResponseEntity<ApiResponse<WarehouseResponse>> getWarehouseById(
            @Parameter(description = "Warehouse ID")
            @PathVariable Long warehouseId) {
        try {
            Warehouse warehouse = warehouseService.getWarehouseById(warehouseId);
            return ResponseEntity.ok(ApiResponse.success(
                    WarehouseResponse.fromEntity(warehouse),
                    "Warehouse retrieved successfully"
            ));
        } catch (RuntimeException e) {
            log.error("Warehouse not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving warehouse", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving warehouse: " + e.getMessage()));
        }
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'OPERATOR')")
    @Operation(summary = "Get warehouse by code", description = "Retrieve warehouse details by code")
    public ResponseEntity<ApiResponse<WarehouseResponse>> getWarehouseByCode(
            @Parameter(description = "Warehouse code")
            @PathVariable String code) {
        try {
            Warehouse warehouse = warehouseService.getWarehouseByCode(code);
            return ResponseEntity.ok(ApiResponse.success(
                    WarehouseResponse.fromEntity(warehouse),
                    "Warehouse retrieved successfully"
            ));
        } catch (RuntimeException e) {
            log.error("Warehouse not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving warehouse", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving warehouse: " + e.getMessage()));
        }
    }

    @PutMapping("/{warehouseId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Update warehouse", description = "Update warehouse details")
    public ResponseEntity<ApiResponse<WarehouseResponse>> updateWarehouse(
            @Parameter(description = "Warehouse ID")
            @PathVariable Long warehouseId,
            @Valid @RequestBody UpdateWarehouseRequest request) {
        try {
            Warehouse updatedData = Warehouse.builder()
                    .name(request.getName())
                    .type(request.getType() != null ? 
                          Warehouse.WarehouseType.valueOf(request.getType()) : null)
                    .address(request.getAddress())
                    .city(request.getCity())
                    .state(request.getState())
                    .country(request.getCountry())
                    .postalCode(request.getPostalCode())
                    .phone(request.getPhone())
                    .email(request.getEmail())
                    .manager(request.getManagerId() != null ? 
                            com.allocat.auth.entity.User.builder().id(request.getManagerId()).build() : null)
                    .isActive(request.getIsActive())
                    .settings(request.getSettings())
                    .build();

            Warehouse updated = warehouseService.updateWarehouse(warehouseId, updatedData);
            return ResponseEntity.ok(ApiResponse.success(
                    WarehouseResponse.fromEntity(updated),
                    "Warehouse updated successfully"
            ));
        } catch (RuntimeException e) {
            log.error("Error updating warehouse: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error updating warehouse", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error updating warehouse: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{warehouseId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Delete warehouse", description = "Deactivate a warehouse (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteWarehouse(
            @Parameter(description = "Warehouse ID")
            @PathVariable Long warehouseId) {
        try {
            warehouseService.deleteWarehouse(warehouseId);
            return ResponseEntity.ok(ApiResponse.success(null, "Warehouse deactivated successfully"));
        } catch (RuntimeException e) {
            log.error("Error deleting warehouse: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error deleting warehouse", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting warehouse: " + e.getMessage()));
        }
    }

    @GetMapping("/{warehouseId}/locations")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'OPERATOR')")
    @Operation(summary = "Get warehouse locations", description = "Retrieve all locations within a warehouse")
    public ResponseEntity<ApiResponse<List<WarehouseLocationResponse>>> getWarehouseLocations(
            @Parameter(description = "Warehouse ID")
            @PathVariable Long warehouseId) {
        try {
            List<WarehouseLocation> locations = warehouseService.getLocationsByWarehouse(warehouseId);
            List<WarehouseLocationResponse> responses = locations.stream()
                    .map(WarehouseLocationResponse::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(
                    responses,
                    "Warehouse locations retrieved successfully"
            ));
        } catch (Exception e) {
            log.error("Error retrieving warehouse locations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving warehouse locations: " + e.getMessage()));
        }
    }

    @PostMapping("/{warehouseId}/locations")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Create warehouse location", description = "Create a new location within a warehouse")
    public ResponseEntity<ApiResponse<WarehouseLocationResponse>> createLocation(
            @Parameter(description = "Warehouse ID")
            @PathVariable Long warehouseId,
            @Valid @RequestBody CreateWarehouseLocationRequest request) {
        try {
            WarehouseLocation location = WarehouseLocation.builder()
                    .warehouse(Warehouse.builder().id(warehouseId).build())
                    .code(request.getCode())
                    .name(request.getName())
                    .locationType(request.getLocationType() != null ? 
                                 WarehouseLocation.LocationType.valueOf(request.getLocationType()) : 
                                 WarehouseLocation.LocationType.BIN)
                    .parentLocation(request.getParentLocationId() != null ? 
                                   WarehouseLocation.builder().id(request.getParentLocationId()).build() : null)
                    .capacityLimit(request.getCapacityLimit())
                    .notes(request.getNotes())
                    .isActive(true)
                    .build();

            WarehouseLocation created = warehouseService.createLocation(location);
            return ResponseEntity.ok(ApiResponse.success(
                    WarehouseLocationResponse.fromEntity(created),
                    "Warehouse location created successfully"
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error creating warehouse location: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error creating warehouse location", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error creating warehouse location: " + e.getMessage()));
        }
    }
}
