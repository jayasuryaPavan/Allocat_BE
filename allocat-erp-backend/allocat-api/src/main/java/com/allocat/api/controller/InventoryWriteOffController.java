package com.allocat.api.controller;

import com.allocat.inventory.dto.WriteOffRequest;
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
@Tag(name = "Inventory Write-off", description = "APIs for recording inventory write-offs")
public class InventoryWriteOffController {

    private final InventoryMovementService inventoryMovementService;

    @PostMapping("/write-off")
    @Operation(summary = "Record inventory write-off")
    public ResponseEntity<ApiResponse<Inventory>> recordWriteOff(@RequestBody WriteOffRequest request) {
        return ResponseEntity.ok(ApiResponse.success(inventoryMovementService.recordWriteOff(request),
                "Write-off recorded successfully"));
    }
}
