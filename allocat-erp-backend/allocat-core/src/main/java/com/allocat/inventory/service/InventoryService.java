package com.allocat.inventory.service;

import com.allocat.auth.entity.Store;
import com.allocat.auth.repository.StoreRepository;
import com.allocat.inventory.entity.Inventory;
import com.allocat.inventory.entity.Product;
import com.allocat.inventory.entity.ReceivedStock;
import com.allocat.inventory.repository.InventoryRepository;
import com.allocat.inventory.repository.ProductRepository;
import com.allocat.inventory.repository.ReceivedStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final ReceivedStockRepository receivedStockRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public Inventory verifyAndAddToInventory(Long receivedStockId, Integer verifiedQuantity, String verifiedBy) {
        ReceivedStock receivedStock = receivedStockRepository.findById(receivedStockId)
                .orElseThrow(() -> new RuntimeException("Received stock not found"));

        if (receivedStock.getStatus() != ReceivedStock.ReceivedStockStatus.PENDING) {
            throw new RuntimeException("Received stock is not in PENDING status");
        }

        // Update received stock with verified quantity
        receivedStock.setVerifiedQuantity(verifiedQuantity);
        receivedStock.setVerifiedBy(verifiedBy);
        receivedStock.setVerifiedDate(LocalDateTime.now());

        // Determine status based on quantity comparison
        if (verifiedQuantity.equals(receivedStock.getExpectedQuantity())) {
            receivedStock.setStatus(ReceivedStock.ReceivedStockStatus.VERIFIED);
        } else if (verifiedQuantity < receivedStock.getExpectedQuantity()) {
            receivedStock.setStatus(ReceivedStock.ReceivedStockStatus.DISCREPANCY);
            receivedStock.setShortageQuantity(receivedStock.getExpectedQuantity() - verifiedQuantity);
        } else {
            receivedStock.setStatus(ReceivedStock.ReceivedStockStatus.DISCREPANCY);
            receivedStock.setExcessQuantity(verifiedQuantity - receivedStock.getExpectedQuantity());
        }

        receivedStockRepository.save(receivedStock);

        // Add to inventory
        return addToInventory(receivedStock, verifiedQuantity);
    }

    @Transactional
    public Inventory addToInventory(ReceivedStock receivedStock, Integer quantity) {
        Product product = receivedStock.getProduct();
        
        // Find existing inventory record for this product
        Optional<Inventory> existingInventory = inventoryRepository.findByProductId(product.getId());
        
        Inventory inventory;
        if (existingInventory.isPresent()) {
            inventory = existingInventory.get();
            // Update existing inventory
            inventory.setCurrentQuantity(inventory.getCurrentQuantity() + quantity);
            inventory.setTotalValue(inventory.getTotalValue().add(
                receivedStock.getUnitPrice().multiply(BigDecimal.valueOf(quantity))
            ));
            inventory.setLastUpdated(LocalDateTime.now());
            inventory.setLastUpdatedBy(receivedStock.getVerifiedBy());
        } else {
            // Create new inventory record
            Store defaultStore = getDefaultStore();
            inventory = Inventory.builder()
                    .product(product)
                    .store(defaultStore)
                    .currentQuantity(quantity)
                    .reservedQuantity(0)
                    .availableQuantity(quantity)
                    .unitCost(receivedStock.getUnitPrice())
                    .totalValue(receivedStock.getUnitPrice().multiply(BigDecimal.valueOf(quantity)))
                    .lastUpdated(LocalDateTime.now())
                    .lastUpdatedBy(receivedStock.getVerifiedBy())
                    .location("Main Warehouse") // Default location
                    .warehouse("Main Warehouse")
                    .batchNumber(receivedStock.getBatchNumber())
                    .supplierName(receivedStock.getSupplierName())
                    .receivedStockId(receivedStock.getId())
                    .notes(receivedStock.getNotes())
                    .build();
        }

        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory updateInventoryQuantity(Long productId, Integer quantityChange, String updatedBy, String reason) {
        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductId(productId);
        
        if (inventoryOpt.isEmpty()) {
            throw new RuntimeException("Inventory record not found for product ID: " + productId);
        }

        Inventory inventory = inventoryOpt.get();
        int newQuantity = inventory.getCurrentQuantity() + quantityChange;
        
        if (newQuantity < 0) {
            throw new RuntimeException("Insufficient inventory. Current: " + inventory.getCurrentQuantity() + 
                                    ", Requested change: " + quantityChange);
        }

        inventory.setCurrentQuantity(newQuantity);
        inventory.setLastUpdated(LocalDateTime.now());
        inventory.setLastUpdatedBy(updatedBy);
        
        // Update total value based on unit cost
        if (inventory.getUnitCost() != null) {
            inventory.setTotalValue(inventory.getUnitCost().multiply(BigDecimal.valueOf(newQuantity)));
        }

        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory reserveInventory(Long productId, Integer quantity, String reservedBy) {
        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductId(productId);
        
        if (inventoryOpt.isEmpty()) {
            throw new RuntimeException("Inventory record not found for product ID: " + productId);
        }

        Inventory inventory = inventoryOpt.get();
        
        if (inventory.getAvailableQuantity() < quantity) {
            throw new RuntimeException("Insufficient available inventory. Available: " + 
                                    inventory.getAvailableQuantity() + ", Requested: " + quantity);
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        inventory.setLastUpdated(LocalDateTime.now());
        inventory.setLastUpdatedBy(reservedBy);

        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory releaseReservation(Long productId, Integer quantity, String releasedBy) {
        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductId(productId);
        
        if (inventoryOpt.isEmpty()) {
            throw new RuntimeException("Inventory record not found for product ID: " + productId);
        }

        Inventory inventory = inventoryOpt.get();
        
        if (inventory.getReservedQuantity() < quantity) {
            throw new RuntimeException("Insufficient reserved inventory. Reserved: " + 
                                    inventory.getReservedQuantity() + ", Requested release: " + quantity);
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
        inventory.setLastUpdated(LocalDateTime.now());
        inventory.setLastUpdatedBy(releasedBy);

        return inventoryRepository.save(inventory);
    }

    public List<Inventory> getLowStockItems() {
        return inventoryRepository.findLowStockItems();
    }

    public List<Inventory> getOverstockItems() {
        return inventoryRepository.findOverstockItems();
    }

    public List<Inventory> getAvailableItems() {
        return inventoryRepository.findAvailableItems();
    }

    public List<Inventory> getOutOfStockItems() {
        return inventoryRepository.findOutOfStockItems();
    }

    public Optional<Inventory> getInventoryByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId);
    }

    public Optional<Inventory> getInventoryByProductCode(String productCode) {
        return inventoryRepository.findByProductCode(productCode);
    }

    public List<Inventory> getInventoryByLocation(String location) {
        return inventoryRepository.findByLocation(location);
    }

    public List<Inventory> getInventoryByWarehouse(String warehouse) {
        return inventoryRepository.findByWarehouse(warehouse);
    }

    public List<ReceivedStock> getAllReceivedStocks() {
        return receivedStockRepository.findAll();
    }

    public List<ReceivedStock> getPendingReceivedStocks() {
        return receivedStockRepository.findPendingReceivedStock();
    }

    public List<ReceivedStock> getReceivedStocksByStatus(ReceivedStock.ReceivedStockStatus status) {
        return receivedStockRepository.findByStatus(status);
    }

    public List<ReceivedStock> getDiscrepancies() {
        return receivedStockRepository.findDiscrepancies();
    }

    /**
     * Get the default store for inventory operations.
     * If no default store is found, returns the first active store.
     * If no stores exist, throws an exception.
     */
    private Store getDefaultStore() {
        // Try to find a store with a specific code (e.g., "MAIN" or "DEFAULT")
        Optional<Store> defaultStore = storeRepository.findByCode("MAIN");
        if (defaultStore.isPresent() && defaultStore.get().getIsActive()) {
            return defaultStore.get();
        }

        // Try "DEFAULT" code as fallback
        defaultStore = storeRepository.findByCode("DEFAULT");
        if (defaultStore.isPresent() && defaultStore.get().getIsActive()) {
            return defaultStore.get();
        }

        // Fallback: get the first active store
        List<Store> activeStores = storeRepository.findByIsActive(true);
        if (!activeStores.isEmpty()) {
            return activeStores.get(0);
        }

        // If no active stores exist, throw an exception
        throw new RuntimeException("No active stores found. Please create at least one active store before adding inventory.");
    }
}

