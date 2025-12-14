package com.allocat.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTOs for Analytics responses
 */
public class AnalyticsDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesSummary {
        private LocalDate date;
        private BigDecimal totalSales;
        private Long transactionCount;
        private BigDecimal averageTicket;
        private BigDecimal taxCollected;
        private BigDecimal discountsGiven;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData {
        private String period; // e.g., "2023-11-26" or "Week 47"
        private BigDecimal sales;
        private Long transactions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CashierPerformance {
        private Long cashierId;
        private String cashierName;
        private Long transactionCount;
        private BigDecimal totalSales;
        private BigDecimal averageTicket;
        private BigDecimal averageTransactionTime; // in minutes
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductAnalytics {
        private Long productId;
        private String productName;
        private String sku;
        private Long quantitySold;
        private BigDecimal revenue;
        private Long transactionCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LowStockAlert {
        private Long productId;
        private String productName;
        private String sku;
        private Integer currentStock;
        private Integer reorderLevel;
        private String storeName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesTrendResponse {
        private List<TrendData> trends;
        private String periodType; // "daily", "weekly", "monthly"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProductsResponse {
        private List<ProductAnalytics> products;
        private String sortBy; // "quantity", "revenue"
        private Integer limit;
    }
}
