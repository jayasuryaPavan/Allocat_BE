package com.allocat.inventory.service;

import com.allocat.inventory.dto.MoveToAisleRequest;
import com.allocat.inventory.dto.ReturnToStorageRequest;
import com.allocat.inventory.dto.WriteOffRequest;
import com.allocat.inventory.exception.InsufficientStockException;
import com.allocat.common.exception.ResourceNotFoundException;
import com.allocat.inventory.entity.Aisle;
import com.allocat.inventory.entity.Inventory;
import com.allocat.inventory.entity.InventoryMovement;
import com.allocat.inventory.entity.InventoryWriteOff;
import com.allocat.inventory.repository.AisleRepository;
import com.allocat.inventory.repository.InventoryMovementRepository;
import com.allocat.inventory.repository.InventoryRepository;
import com.allocat.inventory.repository.InventoryWriteOffRepository;
import com.allocat.auth.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryMovementService {

    private final InventoryRepository inventoryRepository;
    private final AisleRepository aisleRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryWriteOffRepository inventoryWriteOffRepository;

    @Transactional
    public Inventory moveToAisle(MoveToAisleRequest request) {
        Inventory inventory = inventoryRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for product ID: " + request.getProductId()));

        Aisle aisle = aisleRepository.findById(request.getAisleId())
                .orElseThrow(() -> new ResourceNotFoundException("Aisle not found"));

        if (inventory.getStorageQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(
                    "Insufficient storage quantity. Available: " + inventory.getStorageQuantity());
        }

        // Update quantities
        inventory.setStorageQuantity(inventory.getStorageQuantity() - request.getQuantity());
        inventory.setAisleQuantity(inventory.getAisleQuantity() + request.getQuantity());
        inventory.setAisle(aisle); // Update current aisle location assignment if logic dictates single aisle per
                                   // inventory record
        // Note: If a product can be in multiple aisles, the Inventory entity structure
        // might need adjustment.
        // Current plan assumes one Inventory record means one primary location or split
        // qty.
        // But if we want to track WHICH aisle the aisleQuantity is in, we need the
        // reference.
        // If it can be in multiple aisles, we'd need a separate InventoryAisle table.
        // Based on plan, we added "aisle_id" to Inventory, implying 1 active aisle per
        // inventory/product record.

        inventoryRepository.save(inventory);

        // Record movement
        InventoryMovement movement = InventoryMovement.builder()
                .inventory(inventory)
                .product(inventory.getProduct())
                .fromLocation(InventoryMovement.LocationType.STORAGE)
                .toLocation(InventoryMovement.LocationType.AISLE)
                .toAisle(aisle)
                .quantity(request.getQuantity())
                .movedBy(SecurityUtils.getCurrentUserId())
                .notes(request.getNotes())
                .build();

        inventoryMovementRepository.save(movement);

        return inventory;
    }

    @Transactional
    public Inventory returnToStorage(ReturnToStorageRequest request) {
        Inventory inventory = inventoryRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for product ID: " + request.getProductId()));

        if (inventory.getAisleQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(
                    "Insufficient aisle quantity. Available: " + inventory.getAisleQuantity());
        }

        // Update quantities
        inventory.setAisleQuantity(inventory.getAisleQuantity() - request.getQuantity());
        inventory.setStorageQuantity(inventory.getStorageQuantity() + request.getQuantity());

        // If aisle quantity becomes 0, maybe clear the aisle reference? Optional.
        // inventory.setAisle(null);

        inventoryRepository.save(inventory);

        // Record movement
        InventoryMovement movement = InventoryMovement.builder()
                .inventory(inventory)
                .product(inventory.getProduct())
                .fromLocation(InventoryMovement.LocationType.AISLE)
                .toLocation(InventoryMovement.LocationType.STORAGE)
                .fromAisle(inventory.getAisle())
                .quantity(request.getQuantity())
                .movedBy(SecurityUtils.getCurrentUserId())
                .notes(request.getNotes())
                .build();

        inventoryMovementRepository.save(movement);

        return inventory;
    }

    @Transactional
    public Inventory recordWriteOff(WriteOffRequest request) {
        Inventory inventory = inventoryRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for product ID: " + request.getProductId()));

        // Deduct from appropriate location
        if (request.getLocation() == InventoryMovement.LocationType.STORAGE) {
            if (inventory.getStorageQuantity() < request.getQuantity()) {
                throw new InsufficientStockException("Insufficient storage quantity for write-off.");
            }
            inventory.setStorageQuantity(inventory.getStorageQuantity() - request.getQuantity());
        } else {
            if (inventory.getAisleQuantity() < request.getQuantity()) {
                throw new InsufficientStockException("Insufficient aisle quantity for write-off.");
            }
            inventory.setAisleQuantity(inventory.getAisleQuantity() - request.getQuantity());
        }

        inventoryRepository.save(inventory);

        // Record Write-off
        InventoryWriteOff writeOff = InventoryWriteOff.builder()
                .inventory(inventory)
                .product(inventory.getProduct())
                .location(request.getLocation())
                .aisle(request.getAisleId() != null ? aisleRepository.getReferenceById(request.getAisleId())
                        : inventory.getAisle())
                .quantity(request.getQuantity())
                .reason(request.getReason())
                .description(request.getDescription())
                .writtenOffBy(SecurityUtils.getCurrentUserId())
                .build();

        inventoryWriteOffRepository.save(writeOff);

        return inventory;
    }
}
