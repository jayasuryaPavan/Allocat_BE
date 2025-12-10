package com.allocat.inventory.service;

import com.allocat.inventory.entity.Inventory;
import com.allocat.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for multi-store inventory synchronization
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventorySyncService {

    private final InventoryRepository inventoryRepository;

    /**
     * Get inventory levels across all stores for a product
     */
    public Map<Long, Integer> getInventoryAcrossStores(Long productId) {
        List<Inventory> inventories = inventoryRepository.findAll().stream()
                .filter(inv -> inv.getProduct().getId().equals(productId))
                .collect(Collectors.toList());

        Map<Long, Integer> storeInventory = new HashMap<>();

        for (Inventory inventory : inventories) {
            storeInventory.put(
                    inventory.getStore().getId(),
                    inventory.getAvailableQuantity());
        }

        return storeInventory;
    }

    /**
     * Transfer stock between stores
     */
    public boolean transferStock(Long productId, Long fromStoreId, Long toStoreId, Integer quantity) {
        try {
            // Get source inventory
            List<Inventory> fromInventories = inventoryRepository.findAll().stream()
                    .filter(inv -> inv.getProduct().getId().equals(productId)
                            && inv.getStore().getId().equals(fromStoreId))
                    .collect(Collectors.toList());

            if (fromInventories.isEmpty()) {
                log.error("Source inventory not found");
                return false;
            }

            Inventory fromInventory = fromInventories.get(0);

            if (fromInventory.getAvailableQuantity() < quantity) {
                log.error("Insufficient stock in source store");
                return false;
            }

            // Get destination inventory
            List<Inventory> toInventories = inventoryRepository.findAll().stream()
                    .filter(inv -> inv.getProduct().getId().equals(productId)
                            && inv.getStore().getId().equals(toStoreId))
                    .collect(Collectors.toList());

            if (toInventories.isEmpty()) {
                log.error("Destination inventory not found");
                return false;
            }

            Inventory toInventory = toInventories.get(0);

            // Update quantities
            fromInventory.setCurrentQuantity(fromInventory.getCurrentQuantity() - quantity);
            toInventory.setCurrentQuantity(toInventory.getCurrentQuantity() + quantity);

            // Save
            inventoryRepository.save(fromInventory);
            inventoryRepository.save(toInventory);

            log.info("Transferred {} units of product {} from store {} to store {}",
                    quantity, productId, fromStoreId, toStoreId);

            return true;
        } catch (Exception e) {
            log.error("Failed to transfer stock", e);
            return false;
        }
    }
}
