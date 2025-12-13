package com.allocat.inventory.service;

import com.allocat.inventory.dto.ReceivedStockRequest;
import com.allocat.inventory.entity.Product;
import com.allocat.inventory.entity.ReceivedStock;
import com.allocat.inventory.repository.ProductRepository;
import com.allocat.inventory.repository.ReceivedStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReceivedStockService {

    private final ProductRepository productRepository;
    private final ReceivedStockRepository receivedStockRepository;

    public List<ReceivedStock> processReceivedStockList(List<ReceivedStockRequest> requests) {
        String uploadId = UUID.randomUUID().toString();
        List<ReceivedStock> receivedStocks = new ArrayList<>();

        // region agent log
        try (var fw = new java.io.FileWriter("c:/Work Space/Allocat/.cursor/debug.log", true)) {
            var log = new java.util.HashMap<String, Object>();
            log.put("sessionId", "debug-session");
            log.put("runId", "run1");
            log.put("hypothesisId", "H-RS-1");
            log.put("location", "ReceivedStockService:processReceivedStockList:start");
            log.put("message", "Processing received stock list");
            log.put("data", java.util.Map.of("requestCount", requests != null ? requests.size() : 0, "uploadId", uploadId));
            log.put("timestamp", System.currentTimeMillis());
            fw.write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(log) + "\n");
        } catch (Exception ignored) {}
        // endregion

        int rowNumber = 1;
        for (ReceivedStockRequest request : requests) {
            try {
                ReceivedStock receivedStock = parseReceivedStockRequest(request, uploadId, rowNumber);
                receivedStocks.add(receivedStock);
                // region agent log
                try (var fw = new java.io.FileWriter("c:/Work Space/Allocat/.cursor/debug.log", true)) {
                    var log = new java.util.HashMap<String, Object>();
                    log.put("sessionId", "debug-session");
                    log.put("runId", "run1");
                    log.put("hypothesisId", "H-RS-2");
                    log.put("location", "ReceivedStockService:processReceivedStockList:rowSuccess");
                    log.put("message", "Parsed row");
                    log.put("data", java.util.Map.of("row", rowNumber, "productCode", request.getProductCode(), "productName", request.getProductName()));
                    log.put("timestamp", System.currentTimeMillis());
                    fw.write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(log) + "\n");
                } catch (Exception ignored) {}
                // endregion
                rowNumber++;
            } catch (Exception e) {
                log.error("Error processing received stock at row {}: {}", rowNumber, e.getMessage(), e);
                // region agent log
                try (var fw = new java.io.FileWriter("c:/Work Space/Allocat/.cursor/debug.log", true)) {
                    var log = new java.util.HashMap<String, Object>();
                    log.put("sessionId", "debug-session");
                    log.put("runId", "run1");
                    log.put("hypothesisId", "H-RS-3");
                    log.put("location", "ReceivedStockService:processReceivedStockList:rowError");
                    log.put("message", "Row failed");
                    log.put("data", java.util.Map.of(
                            "row", rowNumber,
                            "productCode", request != null ? request.getProductCode() : null,
                            "productName", request != null ? request.getProductName() : null,
                            "error", e.toString()
                    ));
                    log.put("timestamp", System.currentTimeMillis());
                    fw.write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(log) + "\n");
                } catch (Exception ignored) {}
                // endregion
            }
        }

        // Save all received stocks
        if (!receivedStocks.isEmpty()) {
            receivedStockRepository.saveAll(receivedStocks);
            log.info("Successfully processed {} records from JSON payload", receivedStocks.size());
            // region agent log
            try (var fw = new java.io.FileWriter("c:/Work Space/Allocat/.cursor/debug.log", true)) {
                var log = new java.util.HashMap<String, Object>();
                log.put("sessionId", "debug-session");
                log.put("runId", "run1");
                log.put("hypothesisId", "H-RS-4");
                log.put("location", "ReceivedStockService:processReceivedStockList:saveSuccess");
                log.put("message", "Saved received stocks");
                log.put("data", java.util.Map.of("savedCount", receivedStocks.size(), "uploadId", uploadId));
                log.put("timestamp", System.currentTimeMillis());
                fw.write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(log) + "\n");
            } catch (Exception ignored) {}
            // endregion
        } else {
            // region agent log
            try (var fw = new java.io.FileWriter("c:/Work Space/Allocat/.cursor/debug.log", true)) {
                var log = new java.util.HashMap<String, Object>();
                log.put("sessionId", "debug-session");
                log.put("runId", "run1");
                log.put("hypothesisId", "H-RS-5");
                log.put("location", "ReceivedStockService:processReceivedStockList:noSaves");
                log.put("message", "No records saved");
                log.put("data", java.util.Map.of("requestCount", requests != null ? requests.size() : 0));
                log.put("timestamp", System.currentTimeMillis());
                fw.write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(log) + "\n");
            } catch (Exception ignored) {}
            // endregion
        }

        return receivedStocks;
    }

    private ReceivedStock parseReceivedStockRequest(ReceivedStockRequest request, String uploadId, int rowNumber) {
        // Find or create product with all available information
        Product product = findOrCreateProduct(request);

        // Create ReceivedStock entity with all available fields
        ReceivedStock.ReceivedStockBuilder builder = ReceivedStock.builder()
                .product(product)
                .productCode(request.getProductCode())
                .productName(request.getProductName())
                .expectedQuantity(request.getExpectedQuantity())
                .unitPrice(request.getUnitPrice())
                .totalValue(request.getUnitPrice() != null && request.getExpectedQuantity() != null
                        ? request.getUnitPrice().multiply(BigDecimal.valueOf(request.getExpectedQuantity()))
                        : null)
                .status(ReceivedStock.ReceivedStockStatus.PENDING)
                .batchNumber(request.getBatchNumber())
                .supplierName(request.getSupplierName())
                .supplierInvoiceNumber(request.getSupplierInvoice())
                .csvUploadId(uploadId)
                .rowNumber(rowNumber)
                .notes(request.getNotes());

        // Set quantities (with defaults)
        builder.receivedQuantity(request.getReceivedQuantity() != null ? request.getReceivedQuantity() : 0);
        builder.verifiedQuantity(request.getVerifiedQuantity() != null ? request.getVerifiedQuantity() : 0);

        // Set optional quantity fields if provided
        if (request.getDamageQuantity() != null) {
            builder.damageQuantity(request.getDamageQuantity());
        }
        if (request.getShortageQuantity() != null) {
            builder.shortageQuantity(request.getShortageQuantity());
        }
        if (request.getExcessQuantity() != null) {
            builder.excessQuantity(request.getExcessQuantity());
        }

        // Set dates
        builder.deliveryDate(request.getDeliveryDate() != null ? request.getDeliveryDate() : LocalDateTime.now());
        builder.receivedDate(LocalDateTime.now());
        if (request.getExpectedDeliveryDate() != null) {
            builder.expectedDeliveryDate(request.getExpectedDeliveryDate());
        }

        // Set personnel information if provided
        if (request.getReceivedBy() != null && !request.getReceivedBy().trim().isEmpty()) {
            builder.receivedBy(request.getReceivedBy());
        }
        if (request.getVerifiedBy() != null && !request.getVerifiedBy().trim().isEmpty()) {
            builder.verifiedBy(request.getVerifiedBy());
        }

        // Set quality issues if provided
        if (request.getQualityIssues() != null && !request.getQualityIssues().trim().isEmpty()) {
            builder.qualityIssues(request.getQualityIssues());
        }

        return builder.build();
    }

    private Product findOrCreateProduct(ReceivedStockRequest request) {
        String productCode = request.getProductCode();

        // Try to find existing product by product code
        return productRepository.findByProductCode(productCode)
                .map(existingProduct -> {
                    // Update existing product with new information if provided
                    boolean needsUpdate = false;

                    // Update basic information
                    if (request.getProductName() != null && !request.getProductName().trim().isEmpty() &&
                            !request.getProductName().equals(existingProduct.getName())) {
                        existingProduct.setName(request.getProductName());
                        needsUpdate = true;
                    }

                    if (request.getDescription() != null && !request.getDescription().trim().isEmpty() &&
                            !request.getDescription().equals(existingProduct.getDescription())) {
                        existingProduct.setDescription(request.getDescription());
                        needsUpdate = true;
                    }

                    if (request.getCategory() != null && !request.getCategory().trim().isEmpty() &&
                            !request.getCategory().equals(existingProduct.getCategory())) {
                        existingProduct.setCategory(request.getCategory());
                        needsUpdate = true;
                    }

                    // Update pricing
                    if (request.getUnitPrice() != null
                            && !request.getUnitPrice().equals(existingProduct.getUnitPrice())) {
                        existingProduct.setUnitPrice(request.getUnitPrice());
                        needsUpdate = true;
                    }

                    if (request.getUnitOfMeasure() != null && !request.getUnitOfMeasure().trim().isEmpty() &&
                            !request.getUnitOfMeasure().equals(existingProduct.getUnitOfMeasure())) {
                        existingProduct.setUnitOfMeasure(request.getUnitOfMeasure());
                        needsUpdate = true;
                    }

                    // Update supplier information
                    if (request.getSupplierName() != null && !request.getSupplierName().trim().isEmpty() &&
                            !request.getSupplierName().equals(existingProduct.getSupplierName())) {
                        existingProduct.setSupplierName(request.getSupplierName());
                        needsUpdate = true;
                    }

                    if (request.getSupplierContact() != null && !request.getSupplierContact().trim().isEmpty() &&
                            !request.getSupplierContact().equals(existingProduct.getSupplierContact())) {
                        existingProduct.setSupplierContact(request.getSupplierContact());
                        needsUpdate = true;
                    }

                    // Update product identifiers
                    if (request.getBarcode() != null && !request.getBarcode().trim().isEmpty() &&
                            !request.getBarcode().equals(existingProduct.getBarcode())) {
                        existingProduct.setBarcode(request.getBarcode());
                        needsUpdate = true;
                    }

                    // Update product attributes
                    if (request.getBrand() != null && !request.getBrand().trim().isEmpty() &&
                            !request.getBrand().equals(existingProduct.getBrand())) {
                        existingProduct.setBrand(request.getBrand());
                        needsUpdate = true;
                    }

                    if (request.getModel() != null && !request.getModel().trim().isEmpty() &&
                            !request.getModel().equals(existingProduct.getModel())) {
                        existingProduct.setModel(request.getModel());
                        needsUpdate = true;
                    }

                    if (request.getColor() != null && !request.getColor().trim().isEmpty() &&
                            !request.getColor().equals(existingProduct.getColor())) {
                        existingProduct.setColor(request.getColor());
                        needsUpdate = true;
                    }

                    if (request.getSize() != null && !request.getSize().trim().isEmpty() &&
                            !request.getSize().equals(existingProduct.getSize())) {
                        existingProduct.setSize(request.getSize());
                        needsUpdate = true;
                    }

                    if (request.getWeight() != null && !request.getWeight().equals(existingProduct.getWeight())) {
                        existingProduct.setWeight(request.getWeight());
                        needsUpdate = true;
                    }

                    if (request.getDimensions() != null && !request.getDimensions().trim().isEmpty() &&
                            !request.getDimensions().equals(existingProduct.getDimensions())) {
                        existingProduct.setDimensions(request.getDimensions());
                        needsUpdate = true;
                    }

                    // Update stock levels
                    if (request.getMinimumStockLevel() != null &&
                            !request.getMinimumStockLevel().equals(existingProduct.getMinimumStockLevel())) {
                        existingProduct.setMinimumStockLevel(request.getMinimumStockLevel());
                        needsUpdate = true;
                    }

                    if (request.getMaximumStockLevel() != null &&
                            !request.getMaximumStockLevel().equals(existingProduct.getMaximumStockLevel())) {
                        existingProduct.setMaximumStockLevel(request.getMaximumStockLevel());
                        needsUpdate = true;
                    }

                    // Update notes
                    if (request.getNotes() != null && !request.getNotes().trim().isEmpty() &&
                            !request.getNotes().equals(existingProduct.getNotes())) {
                        existingProduct.setNotes(request.getNotes());
                        needsUpdate = true;
                    }

                    if (needsUpdate) {
                        log.info("Updating existing product {} with new information", productCode);
                        @SuppressWarnings("null") // Spring Data JPA save() never returns null
                        Product savedProduct = productRepository.save(existingProduct);
                        return savedProduct;
                    }

                    return existingProduct;
                })
                .orElseGet(() -> {
                    // Create new product if not found with all available information
                    log.info("Creating new product with code: {}", productCode);
                    Product.ProductBuilder productBuilder = Product.builder()
                            .productCode(productCode)
                            .name(request.getProductName())
                            .sku(productCode) // Set sku to productCode to avoid null constraint violation
                            .isActive(true);

                    // Set all optional fields if provided
                    if (request.getDescription() != null) {
                        productBuilder.description(request.getDescription());
                    }
                    if (request.getCategory() != null) {
                        productBuilder.category(request.getCategory());
                    }
                    if (request.getUnitPrice() != null) {
                        productBuilder.unitPrice(request.getUnitPrice());
                    }
                    if (request.getUnitOfMeasure() != null) {
                        productBuilder.unitOfMeasure(request.getUnitOfMeasure());
                    }
                    if (request.getSupplierName() != null) {
                        productBuilder.supplierName(request.getSupplierName());
                    }
                    if (request.getSupplierContact() != null) {
                        productBuilder.supplierContact(request.getSupplierContact());
                    }
                    if (request.getBarcode() != null) {
                        productBuilder.barcode(request.getBarcode());
                    }
                    if (request.getBrand() != null) {
                        productBuilder.brand(request.getBrand());
                    }
                    if (request.getModel() != null) {
                        productBuilder.model(request.getModel());
                    }
                    if (request.getColor() != null) {
                        productBuilder.color(request.getColor());
                    }
                    if (request.getSize() != null) {
                        productBuilder.size(request.getSize());
                    }
                    if (request.getWeight() != null) {
                        productBuilder.weight(request.getWeight());
                    }
                    if (request.getDimensions() != null) {
                        productBuilder.dimensions(request.getDimensions());
                    }
                    if (request.getMinimumStockLevel() != null) {
                        productBuilder.minimumStockLevel(request.getMinimumStockLevel());
                    }
                    if (request.getMaximumStockLevel() != null) {
                        productBuilder.maximumStockLevel(request.getMaximumStockLevel());
                    }
                    if (request.getNotes() != null) {
                        productBuilder.notes(request.getNotes());
                    }

                    @SuppressWarnings("null") // Spring Data JPA save() never returns null
                    Product savedProduct = productRepository.save(productBuilder.build());
                    return savedProduct;
                });
    }

    public List<ReceivedStock> getReceivedStocksByUploadId(String csvUploadId) {
        return receivedStockRepository.findByCsvUploadId(csvUploadId);
    }

    public List<ReceivedStock> getPendingReceivedStocks() {
        return receivedStockRepository.findPendingReceivedStock();
    }
}
