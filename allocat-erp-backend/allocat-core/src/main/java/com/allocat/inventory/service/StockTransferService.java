package com.allocat.inventory.service;

import com.allocat.auth.entity.Store;
import com.allocat.auth.entity.User;
import com.allocat.auth.repository.StoreRepository;
import com.allocat.inventory.entity.*;
import com.allocat.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockTransferService {

    private final StockTransferRepository stockTransferRepository;
    private final StockTransferItemRepository stockTransferItemRepository;
    private final InventoryRepository inventoryRepository;
    private final StoreRepository storeRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;

    @Transactional
    public StockTransfer createTransfer(CreateTransferRequest request) {
        log.info("Creating stock transfer from store {} to store {}", 
                 request.getFromStoreId(), request.getToStoreId());

        Store fromStore = storeRepository.findById(request.getFromStoreId())
                .orElseThrow(() -> new RuntimeException("From store not found: " + request.getFromStoreId()));
        
        Store toStore = storeRepository.findById(request.getToStoreId())
                .orElseThrow(() -> new RuntimeException("To store not found: " + request.getToStoreId()));

        if (fromStore.getId().equals(toStore.getId())) {
            throw new RuntimeException("Cannot transfer to the same store");
        }

        Warehouse fromWarehouse = null;
        Warehouse toWarehouse = null;
        if (request.getFromWarehouseId() != null) {
            fromWarehouse = warehouseRepository.findById(request.getFromWarehouseId())
                    .orElseThrow(() -> new RuntimeException("From warehouse not found"));
        }
        if (request.getToWarehouseId() != null) {
            toWarehouse = warehouseRepository.findById(request.getToWarehouseId())
                    .orElseThrow(() -> new RuntimeException("To warehouse not found"));
        }

        // Determine transfer type
        StockTransfer.TransferType transferType = determineTransferType(fromStore, toStore, fromWarehouse, toWarehouse);

        String transferNo = generateTransferNumber(fromStore.getId(), toStore.getId());

        StockTransfer transfer = StockTransfer.builder()
                .transferNo(transferNo)
                .fromStore(fromStore)
                .toStore(toStore)
                .fromWarehouse(fromWarehouse)
                .toWarehouse(toWarehouse)
                .transferType(transferType)
                .status(StockTransfer.TransferStatus.PENDING)
                .priority(request.getPriority() != null ? request.getPriority() : StockTransfer.Priority.NORMAL)
                .notes(request.getNotes())
                .estimatedDeliveryDate(request.getEstimatedDeliveryDate())
                .shippingMethod(request.getShippingMethod())
                .build();

        if (request.getRequestedBy() != null) {
            transfer.setRequestedBy(request.getRequestedBy());
        }

        StockTransfer savedTransfer = stockTransferRepository.save(transfer);

        // Create transfer items
        List<StockTransferItem> items = request.getItems().stream()
                .map(itemRequest -> {
                    Product product = productRepository.findById(itemRequest.getProductId())
                            .orElseThrow(() -> new RuntimeException("Product not found: " + itemRequest.getProductId()));

                    // Verify inventory availability
                    verifyInventoryAvailability(product.getId(), fromStore.getId(), 
                                              fromWarehouse != null ? fromWarehouse.getId() : null,
                                              itemRequest.getQuantity());

                    return StockTransferItem.builder()
                            .transfer(savedTransfer)
                            .product(product)
                            .quantity(itemRequest.getQuantity())
                            .receivedQuantity(0)
                            .damagedQuantity(0)
                            .build();
                })
                .collect(Collectors.toList());

        stockTransferItemRepository.saveAll(items);
        savedTransfer.setItems(items);

        log.info("Stock transfer created: {}", transferNo);
        return savedTransfer;
    }

    @Transactional
    public StockTransfer approveTransfer(Long transferId, Long approvedByUserId) {
        log.info("Approving stock transfer: {}", transferId);

        StockTransfer transfer = stockTransferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found: " + transferId));

        if (transfer.getStatus() != StockTransfer.TransferStatus.PENDING) {
            throw new RuntimeException("Only pending transfers can be approved");
        }

        // Reserve inventory at source
        for (StockTransferItem item : transfer.getItems()) {
            reserveInventoryForTransfer(item, transfer);
        }

        transfer.setStatus(StockTransfer.TransferStatus.APPROVED);
        transfer.setApprovedBy(User.builder().id(approvedByUserId).build());
        transfer.setTransferDate(LocalDateTime.now());

        return stockTransferRepository.save(transfer);
    }

    @Transactional
    public StockTransfer shipTransfer(Long transferId) {
        log.info("Shipping stock transfer: {}", transferId);

        StockTransfer transfer = stockTransferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found: " + transferId));

        if (transfer.getStatus() != StockTransfer.TransferStatus.APPROVED) {
            throw new RuntimeException("Only approved transfers can be shipped");
        }

        // Deduct inventory from source
        for (StockTransferItem item : transfer.getItems()) {
            deductInventoryForTransfer(item, transfer);
        }

        transfer.setStatus(StockTransfer.TransferStatus.IN_TRANSIT);
        transfer.setTransferDate(LocalDateTime.now());

        return stockTransferRepository.save(transfer);
    }

    @Transactional
    public StockTransfer receiveTransfer(Long transferId, Long receivedByUserId, 
                                        List<ReceiveItemRequest> receivedItems) {
        log.info("Receiving stock transfer: {}", transferId);

        StockTransfer transfer = stockTransferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found: " + transferId));

        if (transfer.getStatus() != StockTransfer.TransferStatus.IN_TRANSIT) {
            throw new RuntimeException("Only in-transit transfers can be received");
        }

        // Add inventory to destination
        for (StockTransferItem item : transfer.getItems()) {
            ReceiveItemRequest receiveRequest = receivedItems.stream()
                    .filter(r -> r.getTransferItemId().equals(item.getId()))
                    .findFirst()
                    .orElse(null);

            if (receiveRequest != null) {
                item.setReceivedQuantity(receiveRequest.getReceivedQuantity());
                item.setDamagedQuantity(receiveRequest.getDamagedQuantity() != null ? 
                                       receiveRequest.getDamagedQuantity() : 0);
                
                addInventoryForTransfer(item, transfer, receiveRequest);
            }
        }

        // Check if fully received
        boolean fullyReceived = transfer.getItems().stream()
                .allMatch(item -> item.getReceivedQuantity() + item.getDamagedQuantity() >= item.getQuantity());

        transfer.setStatus(fullyReceived ? 
                          StockTransfer.TransferStatus.RECEIVED : 
                          StockTransfer.TransferStatus.PARTIALLY_RECEIVED);
        transfer.setReceivedBy(User.builder().id(receivedByUserId).build());
        transfer.setReceivedDate(LocalDateTime.now());
        transfer.setActualDeliveryDate(LocalDateTime.now());

        return stockTransferRepository.save(transfer);
    }

    @Transactional
    public StockTransfer cancelTransfer(Long transferId, String reason) {
        log.info("Cancelling stock transfer: {}", transferId);

        StockTransfer transfer = stockTransferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found: " + transferId));

        if (transfer.getStatus() == StockTransfer.TransferStatus.RECEIVED ||
            transfer.getStatus() == StockTransfer.TransferStatus.CANCELLED) {
            throw new RuntimeException("Cannot cancel transfer in status: " + transfer.getStatus());
        }

        // Release reserved inventory if approved
        if (transfer.getStatus() == StockTransfer.TransferStatus.APPROVED) {
            for (StockTransferItem item : transfer.getItems()) {
                releaseReservedInventory(item, transfer);
            }
        }

        transfer.setStatus(StockTransfer.TransferStatus.CANCELLED);
        if (reason != null) {
            transfer.setNotes((transfer.getNotes() != null ? transfer.getNotes() + "\n" : "") + 
                            "Cancelled: " + reason);
        }

        return stockTransferRepository.save(transfer);
    }

    public List<StockTransfer> getTransfersByStore(Long storeId) {
        return stockTransferRepository.findByFromStoreIdAndStatus(storeId, null);
    }

    public List<StockTransfer> getPendingTransfers(Long storeId) {
        return stockTransferRepository.findByStoreIdAndStatus(storeId, StockTransfer.TransferStatus.PENDING);
    }

    public List<StockTransfer> getInTransitTransfers(Long storeId) {
        return stockTransferRepository.findByStoreIdAndStatus(storeId, StockTransfer.TransferStatus.IN_TRANSIT);
    }

    public StockTransfer getTransferById(Long transferId) {
        return stockTransferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found: " + transferId));
    }

    // Helper methods
    private String generateTransferNumber(Long fromStoreId, Long toStoreId) {
        return String.format("TR-%03d-%03d-%s", fromStoreId, toStoreId, 
                           System.currentTimeMillis() / 1000);
    }

    private StockTransfer.TransferType determineTransferType(Store fromStore, Store toStore,
                                                           Warehouse fromWarehouse, Warehouse toWarehouse) {
        if (fromWarehouse != null && toWarehouse != null) {
            return StockTransfer.TransferType.WAREHOUSE_TO_WAREHOUSE;
        } else if (fromWarehouse != null) {
            return StockTransfer.TransferType.WAREHOUSE_TO_STORE;
        } else if (toWarehouse != null) {
            return StockTransfer.TransferType.STORE_TO_WAREHOUSE;
        } else {
            return StockTransfer.TransferType.STORE_TO_STORE;
        }
    }

    private void verifyInventoryAvailability(Long productId, Long storeId, Long warehouseId, Integer quantity) {
        // Implementation depends on your inventory structure
        // This is a placeholder - implement based on your actual inventory model
        log.debug("Verifying inventory availability for product {} at store {} warehouse {}", 
                 productId, storeId, warehouseId);
    }

    private void reserveInventoryForTransfer(StockTransferItem item, StockTransfer transfer) {
        // Reserve inventory at source location
        log.debug("Reserving inventory for transfer item: {}", item.getId());
        // Implementation needed
    }

    private void deductInventoryForTransfer(StockTransferItem item, StockTransfer transfer) {
        // Deduct inventory from source
        log.debug("Deducting inventory for transfer item: {}", item.getId());
        // Implementation needed
    }

    private void addInventoryForTransfer(StockTransferItem item, StockTransfer transfer, 
                                         ReceiveItemRequest receiveRequest) {
        // Add inventory to destination
        log.debug("Adding inventory for transfer item: {}", item.getId());
        // Implementation needed
    }

    private void releaseReservedInventory(StockTransferItem item, StockTransfer transfer) {
        // Release reserved inventory
        log.debug("Releasing reserved inventory for transfer item: {}", item.getId());
        // Implementation needed
    }

    // DTOs for requests
    public static class CreateTransferRequest {
        private Long fromStoreId;
        private Long toStoreId;
        private Long fromWarehouseId;
        private Long toWarehouseId;
        private Long fromLocationId;
        private Long toLocationId;
        private StockTransfer.Priority priority;
        private String notes;
        private LocalDateTime estimatedDeliveryDate;
        private String shippingMethod;
        private Long requestedBy;
        private List<TransferItemRequest> items;

        // Getters and setters
        public Long getFromStoreId() { return fromStoreId; }
        public void setFromStoreId(Long fromStoreId) { this.fromStoreId = fromStoreId; }
        public Long getToStoreId() { return toStoreId; }
        public void setToStoreId(Long toStoreId) { this.toStoreId = toStoreId; }
        public Long getFromWarehouseId() { return fromWarehouseId; }
        public void setFromWarehouseId(Long fromWarehouseId) { this.fromWarehouseId = fromWarehouseId; }
        public Long getToWarehouseId() { return toWarehouseId; }
        public void setToWarehouseId(Long toWarehouseId) { this.toWarehouseId = toWarehouseId; }
        public Long getFromLocationId() { return fromLocationId; }
        public void setFromLocationId(Long fromLocationId) { this.fromLocationId = fromLocationId; }
        public Long getToLocationId() { return toLocationId; }
        public void setToLocationId(Long toLocationId) { this.toLocationId = toLocationId; }
        public StockTransfer.Priority getPriority() { return priority; }
        public void setPriority(StockTransfer.Priority priority) { this.priority = priority; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public LocalDateTime getEstimatedDeliveryDate() { return estimatedDeliveryDate; }
        public void setEstimatedDeliveryDate(LocalDateTime estimatedDeliveryDate) { this.estimatedDeliveryDate = estimatedDeliveryDate; }
        public String getShippingMethod() { return shippingMethod; }
        public void setShippingMethod(String shippingMethod) { this.shippingMethod = shippingMethod; }
        public Long getRequestedBy() { return requestedBy; }
        public void setRequestedBy(Long requestedBy) { this.requestedBy = requestedBy; }
        public List<TransferItemRequest> getItems() { return items; }
        public void setItems(List<TransferItemRequest> items) { this.items = items; }
    }

    public static class TransferItemRequest {
        private Long productId;
        private Integer quantity;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public static class ReceiveItemRequest {
        private Long transferItemId;
        private Integer receivedQuantity;
        private Integer damagedQuantity;

        public Long getTransferItemId() { return transferItemId; }
        public void setTransferItemId(Long transferItemId) { this.transferItemId = transferItemId; }
        public Integer getReceivedQuantity() { return receivedQuantity; }
        public void setReceivedQuantity(Integer receivedQuantity) { this.receivedQuantity = receivedQuantity; }
        public Integer getDamagedQuantity() { return damagedQuantity; }
        public void setDamagedQuantity(Integer damagedQuantity) { this.damagedQuantity = damagedQuantity; }
    }
}
