package com.allocat.api.controller;

import com.allocat.api.dto.stocktransfer.*;
import com.allocat.common.dto.ApiResponse;
import com.allocat.inventory.entity.StockTransfer;
import com.allocat.inventory.service.StockTransferService;
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
import com.allocat.auth.entity.UserStoreAccess;
import com.allocat.auth.service.AccessControlService;
import com.allocat.auth.util.SecurityUtils;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stock-transfers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Stock Transfer Management", description = "APIs for managing stock transfers between stores and warehouses")
@SecurityRequirement(name = "Bearer Authentication")
public class StockTransferController {

    private final StockTransferService stockTransferService;
    private final AccessControlService accessControlService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'OPERATOR')")
    @Operation(summary = "Create stock transfer", description = "Create a new stock transfer request")
    public ResponseEntity<ApiResponse<StockTransferResponse>> createTransfer(
            @Valid @RequestBody CreateStockTransferRequest request) {
        try {
            // Verify user has access to both stores
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId != null) {
                accessControlService.verifyStoreAccess(
                    userId, 
                    request.getFromStoreId(), 
                    UserStoreAccess.AccessLevel.OPERATE
                );
                accessControlService.verifyStoreAccess(
                    userId, 
                    request.getToStoreId(), 
                    UserStoreAccess.AccessLevel.VIEW
                );
            }
            StockTransferService.CreateTransferRequest serviceRequest = 
                    new StockTransferService.CreateTransferRequest();
            serviceRequest.setFromStoreId(request.getFromStoreId());
            serviceRequest.setToStoreId(request.getToStoreId());
            serviceRequest.setFromWarehouseId(request.getFromWarehouseId());
            serviceRequest.setToWarehouseId(request.getToWarehouseId());
            serviceRequest.setFromLocationId(request.getFromLocationId());
            serviceRequest.setToLocationId(request.getToLocationId());
            serviceRequest.setPriority(request.getPriority() != null ? 
                    StockTransfer.Priority.valueOf(request.getPriority()) : null);
            serviceRequest.setNotes(request.getNotes());
            serviceRequest.setEstimatedDeliveryDate(request.getEstimatedDeliveryDate());
            serviceRequest.setShippingMethod(request.getShippingMethod());

            // Get current user ID from security context
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
                // Extract user ID from principal - adjust based on your implementation
                // For now, setting to null - you'll need to implement user ID extraction
            }

            List<StockTransferService.TransferItemRequest> items = request.getItems().stream()
                    .map(item -> {
                        StockTransferService.TransferItemRequest serviceItem = 
                                new StockTransferService.TransferItemRequest();
                        serviceItem.setProductId(item.getProductId());
                        serviceItem.setQuantity(item.getQuantity());
                        return serviceItem;
                    })
                    .collect(Collectors.toList());
            serviceRequest.setItems(items);

            StockTransfer transfer = stockTransferService.createTransfer(serviceRequest);
            return ResponseEntity.ok(ApiResponse.success(
                    StockTransferResponse.fromEntity(transfer),
                    "Stock transfer created successfully"
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error creating stock transfer: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error creating stock transfer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error creating stock transfer: " + e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'OPERATOR')")
    @Operation(summary = "Get all stock transfers", description = "Retrieve all stock transfers, optionally filtered by store or status")
    public ResponseEntity<ApiResponse<List<StockTransferResponse>>> getAllTransfers(
            @Parameter(description = "Filter by store ID")
            @RequestParam(required = false) Long storeId,
            @Parameter(description = "Filter by status")
            @RequestParam(required = false) String status) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            List<StockTransfer> transfers;
            
            if (storeId != null) {
                // Verify user has access to this store
                if (userId != null) {
                    accessControlService.verifyStoreAccess(
                        userId, 
                        storeId, 
                        UserStoreAccess.AccessLevel.VIEW
                    );
                }
                transfers = stockTransferService.getTransfersByStore(storeId);
                if (status != null) {
                    transfers = transfers.stream()
                            .filter(t -> t.getStatus().name().equals(status))
                            .collect(Collectors.toList());
                }
            } else {
                // Filter by user's accessible stores
                if (userId != null && !SecurityUtils.hasRole("SUPER_ADMIN") && !SecurityUtils.hasRole("ADMIN")) {
                    List<Long> accessibleStoreIds = accessControlService.getAccessibleStoreIds(userId);
                    if (accessibleStoreIds.isEmpty()) {
                        return ResponseEntity.ok(ApiResponse.success(
                                List.of(),
                                "No transfers accessible"
                        ));
                    }
                    // Get transfers for accessible stores
                    transfers = accessibleStoreIds.stream()
                            .flatMap(sid -> stockTransferService.getTransfersByStore(sid).stream())
                            .distinct()
                            .collect(Collectors.toList());
                    if (status != null) {
                        transfers = transfers.stream()
                                .filter(t -> t.getStatus().name().equals(status))
                                .collect(Collectors.toList());
                    }
                } else {
                    transfers = stockTransferService.getTransfersByStore(null);
                    if (status != null) {
                        transfers = transfers.stream()
                                .filter(t -> t.getStatus().name().equals(status))
                                .collect(Collectors.toList());
                    }
                }
            }

            List<StockTransferResponse> responses = transfers.stream()
                    .map(StockTransferResponse::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(
                    responses,
                    "Stock transfers retrieved successfully"
            ));
        } catch (Exception e) {
            log.error("Error retrieving stock transfers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving stock transfers: " + e.getMessage()));
        }
    }

    @GetMapping("/{transferId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'OPERATOR')")
    @Operation(summary = "Get stock transfer by ID", description = "Retrieve stock transfer details by ID")
    public ResponseEntity<ApiResponse<StockTransferResponse>> getTransferById(
            @Parameter(description = "Transfer ID")
            @PathVariable Long transferId) {
        try {
            StockTransfer transfer = stockTransferService.getTransferById(transferId);
            
            // Verify user has access to at least one of the stores in the transfer
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId != null && !SecurityUtils.hasRole("SUPER_ADMIN") && !SecurityUtils.hasRole("ADMIN")) {
                boolean hasAccess = accessControlService.hasStoreAccess(
                    userId, 
                    transfer.getFromStore().getId(), 
                    UserStoreAccess.AccessLevel.VIEW
                ) || accessControlService.hasStoreAccess(
                    userId, 
                    transfer.getToStore().getId(), 
                    UserStoreAccess.AccessLevel.VIEW
                );
                
                if (!hasAccess) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.error("Access denied to this transfer"));
                }
            }
            
            return ResponseEntity.ok(ApiResponse.success(
                    StockTransferResponse.fromEntity(transfer),
                    "Stock transfer retrieved successfully"
            ));
        } catch (RuntimeException e) {
            log.error("Error retrieving stock transfer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error retrieving stock transfer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving stock transfer: " + e.getMessage()));
        }
    }

    @PostMapping("/{transferId}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Approve stock transfer", description = "Approve a pending stock transfer")
    public ResponseEntity<ApiResponse<StockTransferResponse>> approveTransfer(
            @Parameter(description = "Transfer ID")
            @PathVariable Long transferId) {
        try {
            // Get current user ID from security context
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userId = getCurrentUserId(auth);

            StockTransfer transfer = stockTransferService.approveTransfer(transferId, userId);
            return ResponseEntity.ok(ApiResponse.success(
                    StockTransferResponse.fromEntity(transfer),
                    "Stock transfer approved successfully"
            ));
        } catch (RuntimeException e) {
            log.error("Error approving stock transfer: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error approving stock transfer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error approving stock transfer: " + e.getMessage()));
        }
    }

    @PostMapping("/{transferId}/ship")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'OPERATOR')")
    @Operation(summary = "Ship stock transfer", description = "Mark a stock transfer as shipped")
    public ResponseEntity<ApiResponse<StockTransferResponse>> shipTransfer(
            @Parameter(description = "Transfer ID")
            @PathVariable Long transferId) {
        try {
            StockTransfer transfer = stockTransferService.shipTransfer(transferId);
            return ResponseEntity.ok(ApiResponse.success(
                    StockTransferResponse.fromEntity(transfer),
                    "Stock transfer shipped successfully"
            ));
        } catch (RuntimeException e) {
            log.error("Error shipping stock transfer: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error shipping stock transfer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error shipping stock transfer: " + e.getMessage()));
        }
    }

    @PostMapping("/{transferId}/receive")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'OPERATOR')")
    @Operation(summary = "Receive stock transfer", description = "Receive a shipped stock transfer")
    public ResponseEntity<ApiResponse<StockTransferResponse>> receiveTransfer(
            @Parameter(description = "Transfer ID")
            @PathVariable Long transferId,
            @Valid @RequestBody ReceiveTransferRequest request) {
        try {
            // Get current user ID from security context
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userId = getCurrentUserId(auth);

            List<StockTransferService.ReceiveItemRequest> receivedItems = request.getReceivedItems().stream()
                    .map(item -> {
                        StockTransferService.ReceiveItemRequest serviceItem = 
                                new StockTransferService.ReceiveItemRequest();
                        serviceItem.setTransferItemId(item.getTransferItemId());
                        serviceItem.setReceivedQuantity(item.getReceivedQuantity());
                        serviceItem.setDamagedQuantity(item.getDamagedQuantity());
                        return serviceItem;
                    })
                    .collect(Collectors.toList());

            StockTransfer transfer = stockTransferService.receiveTransfer(transferId, userId, receivedItems);
            return ResponseEntity.ok(ApiResponse.success(
                    StockTransferResponse.fromEntity(transfer),
                    "Stock transfer received successfully"
            ));
        } catch (RuntimeException e) {
            log.error("Error receiving stock transfer: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error receiving stock transfer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error receiving stock transfer: " + e.getMessage()));
        }
    }

    @PostMapping("/{transferId}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Cancel stock transfer", description = "Cancel a stock transfer")
    public ResponseEntity<ApiResponse<StockTransferResponse>> cancelTransfer(
            @Parameter(description = "Transfer ID")
            @PathVariable Long transferId,
            @Parameter(description = "Cancellation reason")
            @RequestParam(required = false) String reason) {
        try {
            StockTransfer transfer = stockTransferService.cancelTransfer(transferId, reason);
            return ResponseEntity.ok(ApiResponse.success(
                    StockTransferResponse.fromEntity(transfer),
                    "Stock transfer cancelled successfully"
            ));
        } catch (RuntimeException e) {
            log.error("Error cancelling stock transfer: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error cancelling stock transfer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error cancelling stock transfer: " + e.getMessage()));
        }
    }

    @GetMapping("/store/{storeId}/pending")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'OPERATOR')")
    @Operation(summary = "Get pending transfers for store", description = "Retrieve all pending transfers for a store")
    public ResponseEntity<ApiResponse<List<StockTransferResponse>>> getPendingTransfers(
            @Parameter(description = "Store ID")
            @PathVariable Long storeId) {
        try {
            List<StockTransfer> transfers = stockTransferService.getPendingTransfers(storeId);
            List<StockTransferResponse> responses = transfers.stream()
                    .map(StockTransferResponse::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(
                    responses,
                    "Pending transfers retrieved successfully"
            ));
        } catch (Exception e) {
            log.error("Error retrieving pending transfers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving pending transfers: " + e.getMessage()));
        }
    }

    @GetMapping("/store/{storeId}/in-transit")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'OPERATOR')")
    @Operation(summary = "Get in-transit transfers for store", description = "Retrieve all in-transit transfers for a store")
    public ResponseEntity<ApiResponse<List<StockTransferResponse>>> getInTransitTransfers(
            @Parameter(description = "Store ID")
            @PathVariable Long storeId) {
        try {
            List<StockTransfer> transfers = stockTransferService.getInTransitTransfers(storeId);
            List<StockTransferResponse> responses = transfers.stream()
                    .map(StockTransferResponse::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(
                    responses,
                    "In-transit transfers retrieved successfully"
            ));
        } catch (Exception e) {
            log.error("Error retrieving in-transit transfers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving in-transit transfers: " + e.getMessage()));
        }
    }

    // Helper method to extract user ID from authentication
    private Long getCurrentUserId(Authentication auth) {
        return SecurityUtils.getCurrentUserId(auth);
    }
}
