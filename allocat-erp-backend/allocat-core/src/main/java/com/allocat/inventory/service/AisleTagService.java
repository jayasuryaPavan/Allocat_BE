package com.allocat.inventory.service;

import com.allocat.common.exception.ResourceNotFoundException;
import com.allocat.inventory.entity.Aisle;
import com.allocat.inventory.entity.Inventory;
import com.allocat.inventory.entity.Product;
import com.allocat.inventory.repository.AisleRepository;
import com.allocat.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AisleTagService {

    private final InventoryRepository inventoryRepository;
    private final AisleRepository aisleRepository;

    public AisleTagData generateTagData(Long productId, Long aisleId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));

        Product product = inventory.getProduct();
        Aisle aisle = null;
        if (aisleId != null) {
            aisle = aisleRepository.findById(aisleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Aisle not found"));
        } else {
            aisle = inventory.getAisle();
        }

        return AisleTagData.builder()
                .productName(product.getName())
                .barcode(product.getBarcode() != null ? product.getBarcode() : product.getProductCode())
                .price(product.getUnitPrice())
                .aisleNumber(aisle != null ? aisle.getAisleNumber() : "N/A")
                .sku(product.getSku())
                .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class AisleTagData {
        private String productName;
        private String barcode;
        private java.math.BigDecimal price;
        private String aisleNumber;
        private String sku;
    }

    // Future: Method to generate actual PDF byte array
    // public byte[] generatePdf(AisleTagData data) { ... }
}
