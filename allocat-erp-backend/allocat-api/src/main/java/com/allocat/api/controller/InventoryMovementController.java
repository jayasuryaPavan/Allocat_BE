package com.allocat.api.controller;

import com.allocat.inventory.dto.MoveToAisleRequest;
import com.allocat.inventory.dto.ReturnToStorageRequest;
import com.allocat.common.dto.ApiResponse;
import com.allocat.inventory.entity.Inventory;
import com.allocat.inventory.service.InventoryMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventory Movement", description = "APIs for moving inventory between storage and aisles")
public class InventoryMovementController {

    private final InventoryMovementService inventoryMovementService;

    @PostMapping("/move-to-aisle")
    @Operation(summary = "Move inventory from storage to aisle")
    public ResponseEntity<ApiResponse<Inventory>> moveToAisle(@RequestBody MoveToAisleRequest request) {
        return ResponseEntity
                .ok(ApiResponse.success(inventoryMovementService.moveToAisle(request), "Moved to aisle successfully"));
    }

    @PostMapping("/return-to-storage")
    @Operation(summary = "Return inventory from aisle to storage")
    public ResponseEntity<ApiResponse<Inventory>> returnToStorage(@RequestBody ReturnToStorageRequest request) {
        return ResponseEntity.ok(ApiResponse.success(inventoryMovementService.returnToStorage(request),
                "Returned to storage successfully"));
    }
}
