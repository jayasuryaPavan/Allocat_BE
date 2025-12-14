package com.allocat.api.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsolidatedInventoryResponse {
    private Long productId;
    private String productCode;
    private String productName;
    private String sku;
    private String barcode;
    private Integer totalQuantity;
    private Integer totalReserved;
    private Integer totalAvailable;
    private Integer storeCount;
    private Integer warehouseCount;
    private Integer minAvailable;
    private Integer maxAvailable;
    private BigDecimal avgUnitCost;
    private BigDecimal totalValue;
}
