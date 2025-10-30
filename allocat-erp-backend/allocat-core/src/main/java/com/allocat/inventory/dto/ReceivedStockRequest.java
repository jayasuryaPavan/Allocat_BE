package com.allocat.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceivedStockRequest {
    // Product Information
    private String productCode;
    private String productName;
    private String description;
    private String category;
    private String barcode;
    private String brand;
    private String model;
    private String color;
    private String size;
    private BigDecimal weight;
    private String dimensions;
    private String unitOfMeasure;
    
    // Quantity Information
    private Integer expectedQuantity;
    private Integer receivedQuantity;
    private Integer verifiedQuantity;
    private Integer damageQuantity;
    private Integer shortageQuantity;
    private Integer excessQuantity;
    
    // Pricing Information
    private BigDecimal unitPrice;
    
    // Supplier Information
    private String supplierName;
    private String supplierContact;
    private String supplierInvoice;
    
    // Batch and Tracking
    private String batchNumber;
    
    // Dates
    private LocalDateTime expectedDeliveryDate;
    private LocalDateTime deliveryDate;
    
    // Personnel
    private String receivedBy;
    private String verifiedBy;
    
    // Notes and Issues
    private String notes;
    private String qualityIssues;
    
    // Stock Levels (for product)
    private Integer minimumStockLevel;
    private Integer maximumStockLevel;
}
