package com.allocat.inventory.service;

import com.allocat.inventory.dto.AisleRequest;
import com.allocat.auth.entity.Store;
import com.allocat.auth.util.SecurityUtils;
import com.allocat.common.exception.ResourceNotFoundException;
import com.allocat.inventory.entity.Aisle;
import com.allocat.inventory.repository.AisleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AisleService {

    private final AisleRepository aisleRepository;

    public List<Aisle> getAislesByStore(Long storeId) {
        // accessControlService.verifyStoreAccess(SecurityUtils.getCurrentUserId(),
        // storeId, VIEW); // Assume managed by Controller or Aspect
        return aisleRepository.findByStoreId(storeId);
    }

    public List<Aisle> getActiveAislesByStore(Long storeId) {
        return aisleRepository.findByStoreIdAndIsActiveTrue(storeId);
    }

    @Transactional
    public Aisle createAisle(Long storeId, AisleRequest request) {
        if (aisleRepository.existsByStoreIdAndAisleNumber(storeId, request.getAisleNumber())) {
            throw new IllegalArgumentException(
                    "Aisle number " + request.getAisleNumber() + " already exists in this store.");
        }

        // Ideally fetch Store entity, here assuming we have a reference or simple ID
        // association if supported
        // For strict JPA, we need to fetch the Store proxy.
        Store store = Store.builder().id(storeId).build(); // Assuming ID-only proxy is enough if not validated deeper
                                                           // here

        Aisle aisle = Aisle.builder()
                .store(store)
                .aisleNumber(request.getAisleNumber())
                .aisleName(request.getAisleName())
                .productType(request.getProductType())
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .createdBy(SecurityUtils.getCurrentUserId())
                .updatedBy(SecurityUtils.getCurrentUserId())
                .build();

        return aisleRepository.save(aisle);
    }

    @Transactional
    public Aisle updateAisle(Long aisleId, AisleRequest request) {
        Aisle aisle = aisleRepository.findById(aisleId)
                .orElseThrow(() -> new ResourceNotFoundException("Aisle not found"));

        if (request.getAisleNumber() != null && !request.getAisleNumber().equals(aisle.getAisleNumber())) {
            if (aisleRepository.existsByStoreIdAndAisleNumber(aisle.getStore().getId(), request.getAisleNumber())) {
                throw new IllegalArgumentException(
                        "Aisle number " + request.getAisleNumber() + " already exists in this store.");
            }
            aisle.setAisleNumber(request.getAisleNumber());
        }

        if (request.getAisleName() != null)
            aisle.setAisleName(request.getAisleName());
        if (request.getProductType() != null)
            aisle.setProductType(request.getProductType());
        if (request.getDescription() != null)
            aisle.setDescription(request.getDescription());
        if (request.getIsActive() != null)
            aisle.setIsActive(request.getIsActive());

        aisle.setUpdatedBy(SecurityUtils.getCurrentUserId());

        return aisleRepository.save(aisle);
    }

    @Transactional
    public void deleteAisle(Long aisleId) {
        Aisle aisle = aisleRepository.findById(aisleId)
                .orElseThrow(() -> new ResourceNotFoundException("Aisle not found"));

        // Check if aisle has inventory
        // This query might need to be refined based on actual Inventory relationship
        // mapping
        // Assuming Inventory has aisle reference
        // long inventoryCount = inventoryRepository.countByAisleId(aisleId);
        // if (inventoryCount > 0) { throw ... }

        // Soft delete
        aisle.setIsActive(false);
        aisleRepository.save(aisle);
    }

    // Suggest aisle based on product category/type
    public Optional<Aisle> suggestAisleForProduct(Long storeId, String productCategory) {
        List<Aisle> aisles = aisleRepository.findByStoreIdAndProductType(storeId, productCategory);
        return aisles.stream().findFirst();
    }
}
