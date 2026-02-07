package com.allocat.api.controller;

import com.allocat.inventory.dto.AisleRequest;
import com.allocat.common.dto.ApiResponse;
import com.allocat.inventory.entity.Aisle;
import com.allocat.inventory.service.AisleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aisles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Aisle Management", description = "APIs for managing store aisles")
public class AisleController {

    private final AisleService aisleService;

    @GetMapping("/store/{storeId}")
    @Operation(summary = "Get all aisles for a store")
    public ResponseEntity<ApiResponse<List<Aisle>>> getAislesByStore(@PathVariable Long storeId) {
        return ResponseEntity.ok(ApiResponse.success(aisleService.getAislesByStore(storeId)));
    }

    @PostMapping("/store/{storeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER')") // Updated permission
    @Operation(summary = "Create a new aisle")
    public ResponseEntity<ApiResponse<Aisle>> createAisle(@PathVariable Long storeId,
            @RequestBody AisleRequest request) {
        return ResponseEntity
                .ok(ApiResponse.success(aisleService.createAisle(storeId, request), "Aisle created successfully"));
    }

    @PutMapping("/{aisleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER')") // Updated permission
    @Operation(summary = "Update an aisle")
    public ResponseEntity<ApiResponse<Aisle>> updateAisle(@PathVariable Long aisleId,
            @RequestBody AisleRequest request) {
        return ResponseEntity
                .ok(ApiResponse.success(aisleService.updateAisle(aisleId, request), "Aisle updated successfully"));
    }

    @DeleteMapping("/{aisleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER')") // Updated permission
    @Operation(summary = "Delete an aisle")
    public ResponseEntity<ApiResponse<Void>> deleteAisle(@PathVariable Long aisleId) {
        aisleService.deleteAisle(aisleId);
        return ResponseEntity.ok(ApiResponse.success(null, "Aisle deleted successfully"));
    }

    @GetMapping("/suggest")
    @Operation(summary = "Suggest aisle for product")
    public ResponseEntity<ApiResponse<Aisle>> suggestAisle(
            @RequestParam Long storeId,
            @RequestParam String productType) {
        return aisleService.suggestAisleForProduct(storeId, productType)
                .map(aisle -> ResponseEntity.ok(ApiResponse.success(aisle, "Aisle suggestion found")))
                .orElse(ResponseEntity.ok(ApiResponse.success(null, "No suggestion found")));
    }
}
