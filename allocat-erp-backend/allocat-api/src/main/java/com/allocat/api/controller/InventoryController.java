package com.allocat.api.controller;

import com.allocat.common.dto.ApiResponse;
import com.allocat.inventory.dto.ReceivedStockRequest;
import com.allocat.inventory.entity.Inventory;
import com.allocat.inventory.entity.ReceivedStock;
import com.allocat.inventory.repository.InventoryRepository;
import com.allocat.inventory.service.ReceivedStockService;
import com.allocat.inventory.service.InventoryService;
import com.allocat.auth.service.AccessControlService;
import com.allocat.auth.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.allocat.auth.entity.UserStoreAccess;
import com.allocat.auth.entity.UserStoreAccess;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventory Management", description = "APIs for inventory allocation and management")
public class InventoryController {

    private final InventoryService inventoryService;
    private final ReceivedStockService receivedStockService;
    private final InventoryRepository inventoryRepository;
    private final AccessControlService accessControlService;

    @PostMapping("/received-stock")
    @Operation(summary = "Add received stock via JSON", 
               description = "Add received stock records by sending a JSON array of product information")
    public ResponseEntity<ApiResponse<List<ReceivedStock>>> addReceivedStock(
            @Parameter(description = "Array of received stock items") 
            @RequestBody List<ReceivedStockRequest> receivedStockList) {
        try {
            if (receivedStockList == null || receivedStockList.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<List<ReceivedStock>>builder()
                                .success(false)
                                .message("Received stock list cannot be empty")
                                .build());
            }

            List<ReceivedStock> savedReceivedStocks = receivedStockService.processReceivedStockList(receivedStockList);
            
            return ResponseEntity.ok(ApiResponse.<List<ReceivedStock>>builder()
                    .success(true)
                    .message("Received stock processed successfully. " + savedReceivedStocks.size() + " records created.")
                    .data(savedReceivedStocks)
                    .build());

        } catch (Exception e) {
            log.error("Error processing received stock", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<ReceivedStock>>builder()
                            .success(false)
                            .message("Error processing received stock: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/received-stock")
    @Operation(summary = "Get all received stock", 
               description = "Retrieve all received stock records (all statuses)")
    public ResponseEntity<ApiResponse<List<ReceivedStock>>> getAllReceivedStock() {
        try {
            List<ReceivedStock> allReceivedStocks = inventoryService.getAllReceivedStocks();
            return ResponseEntity.ok(ApiResponse.<List<ReceivedStock>>builder()
                    .success(true)
                    .message("All received stock retrieved successfully")
                    .data(allReceivedStocks)
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving all received stock", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<ReceivedStock>>builder()
                            .success(false)
                            .message("Error retrieving all received stock: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/received-stock/pending")
    @Operation(summary = "Get all pending received stock", 
               description = "Retrieve all unverified stock that needs to be verified")
    public ResponseEntity<ApiResponse<List<ReceivedStock>>> getPendingReceivedStock() {
        try {
            List<ReceivedStock> pendingStocks = inventoryService.getPendingReceivedStocks();
            return ResponseEntity.ok(ApiResponse.<List<ReceivedStock>>builder()
                    .success(true)
                    .message("Pending received stock retrieved successfully")
                    .data(pendingStocks)
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving pending received stock", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<ReceivedStock>>builder()
                            .success(false)
                            .message("Error retrieving pending received stock: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/received-stock/{receivedStockId}/verify")
    @Operation(summary = "Verify received stock and add to inventory", 
               description = "Verify the received quantity and add the stock to inventory")
    public ResponseEntity<ApiResponse<Inventory>> verifyReceivedStock(
            @Parameter(description = "ID of the received stock to verify") 
            @PathVariable Long receivedStockId,
            @Parameter(description = "Verified quantity received") 
            @RequestParam Integer verifiedQuantity,
            @Parameter(description = "Name of the person verifying the stock") 
            @RequestParam String verifiedBy) {
        try {
            Inventory inventory = inventoryService.verifyAndAddToInventory(receivedStockId, verifiedQuantity, verifiedBy);
            return ResponseEntity.ok(ApiResponse.<Inventory>builder()
                    .success(true)
                    .message("Stock verified and added to inventory successfully")
                    .data(inventory)
                    .build());
        } catch (Exception e) {
            log.error("Error verifying received stock", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Inventory>builder()
                            .success(false)
                            .message("Error verifying received stock: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/current")
    @Operation(summary = "Get current inventory", 
               description = "Retrieve current inventory levels for all products with pagination and sorting")
    public ResponseEntity<ApiResponse<Page<Inventory>>> getCurrentInventory(
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Filter by store ID")
            @RequestParam(required = false) Long storeId,
            @Parameter(description = "Filter by warehouse ID")
            @RequestParam(required = false) Long warehouseId,
            @Parameter(description = "Sort by field (e.g., 'product.name', 'availableQuantity', 'currentQuantity')") 
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction: 'asc' or 'desc'") 
            @RequestParam(defaultValue = "asc") String sortDirection) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            
            // Apply multi-store filtering
            if (storeId != null && userId != null) {
                // Verify user has access to this store
                if (!SecurityUtils.hasRole("SUPER_ADMIN") && !SecurityUtils.hasRole("ADMIN")) {
                    accessControlService.verifyStoreAccess(
                        userId, 
                        storeId, 
                        com.allocat.auth.entity.UserStoreAccess.AccessLevel.VIEW
                    );
                }
            } else if (userId != null && !SecurityUtils.hasRole("SUPER_ADMIN") && !SecurityUtils.hasRole("ADMIN")) {
                // Filter by user's accessible stores
                List<Long> accessibleStoreIds = accessControlService.getAccessibleStoreIds(userId);
                if (accessibleStoreIds.isEmpty()) {
                    return ResponseEntity.ok(ApiResponse.success(
                            Page.empty(),
                            "No inventory accessible"
                    ));
                }
                // Note: You'll need to update InventoryRepository to support store filtering
                // For now, this is a placeholder
            }
            
            // Create sort object
            Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : 
                    Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Inventory> inventory = inventoryRepository.findAvailableItems(pageable);
            
            return ResponseEntity.ok(ApiResponse.<Page<Inventory>>builder()
                    .success(true)
                    .message("Current inventory retrieved successfully. Page " + (page + 1) + " of " + inventory.getTotalPages())
                    .data(inventory)
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving current inventory", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Page<Inventory>>builder()
                            .success(false)
                            .message("Error retrieving current inventory: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get inventory for specific product", 
               description = "Retrieve inventory information for a specific product")
    public ResponseEntity<ApiResponse<Inventory>> getInventoryByProductId(
            @Parameter(description = "Product ID") 
            @PathVariable Long productId) {
        try {
            return inventoryService.getInventoryByProductId(productId)
                    .map(inventory -> ResponseEntity.ok(ApiResponse.<Inventory>builder()
                            .success(true)
                            .message("Product inventory retrieved successfully")
                            .data(inventory)
                            .build()))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error retrieving product inventory", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Inventory>builder()
                            .success(false)
                            .message("Error retrieving product inventory: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock items", 
               description = "Retrieve products that are below minimum stock level with pagination")
    public ResponseEntity<ApiResponse<Page<Inventory>>> getLowStockItems(
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") 
            @RequestParam(defaultValue = "availableQuantity") String sortBy,
            @Parameter(description = "Sort direction: 'asc' or 'desc'") 
            @RequestParam(defaultValue = "asc") String sortDirection) {
        try {
            Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : 
                    Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Inventory> lowStockItems = inventoryRepository.findLowStockItems(pageable);
            
            return ResponseEntity.ok(ApiResponse.<Page<Inventory>>builder()
                    .success(true)
                    .message("Low stock items retrieved successfully. Page " + (page + 1) + " of " + lowStockItems.getTotalPages())
                    .data(lowStockItems)
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving low stock items", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Page<Inventory>>builder()
                            .success(false)
                            .message("Error retrieving low stock items: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/out-of-stock")
    @Operation(summary = "Get out of stock items", 
               description = "Retrieve products that are completely out of stock with pagination")
    public ResponseEntity<ApiResponse<Page<Inventory>>> getOutOfStockItems(
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") 
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction: 'asc' or 'desc'") 
            @RequestParam(defaultValue = "asc") String sortDirection) {
        try {
            Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : 
                    Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Inventory> outOfStockItems = inventoryRepository.findOutOfStockItems(pageable);
            
            return ResponseEntity.ok(ApiResponse.<Page<Inventory>>builder()
                    .success(true)
                    .message("Out of stock items retrieved successfully. Page " + (page + 1) + " of " + outOfStockItems.getTotalPages())
                    .data(outOfStockItems)
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving out of stock items", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Page<Inventory>>builder()
                            .success(false)
                            .message("Error retrieving out of stock items: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/reserve")
    @Operation(summary = "Reserve inventory", 
               description = "Reserve a specific quantity of inventory for a product")
    public ResponseEntity<ApiResponse<Inventory>> reserveInventory(
            @Parameter(description = "Product ID") 
            @RequestParam Long productId,
            @Parameter(description = "Quantity to reserve") 
            @RequestParam Integer quantity,
            @Parameter(description = "Name of the person reserving") 
            @RequestParam String reservedBy) {
        try {
            Inventory inventory = inventoryService.reserveInventory(productId, quantity, reservedBy);
            return ResponseEntity.ok(ApiResponse.<Inventory>builder()
                    .success(true)
                    .message("Inventory reserved successfully")
                    .data(inventory)
                    .build());
        } catch (Exception e) {
            log.error("Error reserving inventory", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Inventory>builder()
                            .success(false)
                            .message("Error reserving inventory: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/release-reservation")
    @Operation(summary = "Release inventory reservation", 
               description = "Release a previously reserved quantity of inventory")
    public ResponseEntity<ApiResponse<Inventory>> releaseReservation(
            @Parameter(description = "Product ID") 
            @RequestParam Long productId,
            @Parameter(description = "Quantity to release") 
            @RequestParam Integer quantity,
            @Parameter(description = "Name of the person releasing") 
            @RequestParam String releasedBy) {
        try {
            Inventory inventory = inventoryService.releaseReservation(productId, quantity, releasedBy);
            return ResponseEntity.ok(ApiResponse.<Inventory>builder()
                    .success(true)
                    .message("Inventory reservation released successfully")
                    .data(inventory)
                    .build());
        } catch (Exception e) {
            log.error("Error releasing inventory reservation", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Inventory>builder()
                            .success(false)
                            .message("Error releasing inventory reservation: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/discrepancies")
    @Operation(summary = "Get stock discrepancies", 
               description = "Retrieve received stock with quantity discrepancies")
    public ResponseEntity<ApiResponse<List<ReceivedStock>>> getDiscrepancies() {
        try {
            List<ReceivedStock> discrepancies = inventoryService.getDiscrepancies();
            return ResponseEntity.ok(ApiResponse.<List<ReceivedStock>>builder()
                    .success(true)
                    .message("Stock discrepancies retrieved successfully")
                    .data(discrepancies)
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving stock discrepancies", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<ReceivedStock>>builder()
                            .success(false)
                            .message("Error retrieving stock discrepancies: " + e.getMessage())
                            .build());
        }
    }
}

