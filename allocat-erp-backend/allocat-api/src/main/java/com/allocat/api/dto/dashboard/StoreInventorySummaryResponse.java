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
public class StoreInventorySummaryResponse {
    private Long storeId;
    private String storeCode;
    private String storeName;
    private Integer productCount;
    private Integer totalQuantity;
    private Integer totalAvailable;
    private BigDecimal totalInventoryValue;
    private Integer warehouseCount;
}
