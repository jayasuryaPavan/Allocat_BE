package com.allocat.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs for Shift and Day Reports
 */
public class ReportDTO {

    // ============== SHIFT REPORT DTOs ==============

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShiftReport {
        private Long shiftId;
        private String employeeName;
        private Long employeeId;
        private String storeName;
        private Long storeId;
        private LocalDate shiftDate;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String status;
        private Long durationMinutes;
        
        // Cash reconciliation
        private BigDecimal startingCash;
        private BigDecimal endingCash;
        private BigDecimal expectedCash;
        private BigDecimal cashDifference;
        
        // Sales summary
        private BigDecimal totalSales;
        private Long totalOrders;
        private BigDecimal averageTicket;
        private BigDecimal taxCollected;
        private BigDecimal discountsGiven;
        
        // Payment breakdown
        private List<PaymentBreakdown> paymentBreakdown;
        
        // Top products
        private List<ProductSold> topProducts;
        
        // Order list (optional, can be paginated)
        private List<OrderSummary> orders;
        
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentBreakdown {
        private String paymentType;
        private BigDecimal amount;
        private Long transactionCount;
        private BigDecimal percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSold {
        private Long productId;
        private String productName;
        private String sku;
        private Long quantitySold;
        private BigDecimal revenue;
        private BigDecimal unitPrice;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderSummary {
        private Long orderId;
        private String orderNo;
        private LocalDateTime orderDate;
        private BigDecimal subtotal;
        private BigDecimal tax;
        private BigDecimal discount;
        private BigDecimal total;
        private String paymentType;
        private String status;
        private Integer itemCount;
    }

    // ============== DAY REPORT DTOs ==============

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayReport {
        private LocalDate date;
        private String storeName;
        private Long storeId;
        
        // Shift summary
        private Integer totalShifts;
        private Integer activeShifts;
        private Integer completedShifts;
        
        // Sales summary
        private BigDecimal totalSales;
        private Long totalOrders;
        private BigDecimal averageTicket;
        private BigDecimal taxCollected;
        private BigDecimal discountsGiven;
        
        // Cash summary across all shifts
        private BigDecimal totalStartingCash;
        private BigDecimal totalEndingCash;
        private BigDecimal totalCashDifference;
        
        // Payment breakdown for the day
        private List<PaymentBreakdown> paymentBreakdown;
        
        // Hourly breakdown
        private List<HourlySales> hourlyBreakdown;
        
        // Top products for the day
        private List<ProductSold> topProducts;
        
        // Employee performance
        private List<EmployeePerformance> employeePerformance;
        
        // Shift details
        private List<ShiftSummary> shifts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlySales {
        private Integer hour;
        private String hourLabel; // e.g., "9:00 AM - 10:00 AM"
        private BigDecimal sales;
        private Long orders;
        private BigDecimal averageTicket;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeePerformance {
        private Long employeeId;
        private String employeeName;
        private BigDecimal totalSales;
        private Long totalOrders;
        private BigDecimal averageTicket;
        private Long shiftCount;
        private Long totalMinutesWorked;
        private BigDecimal salesPerHour;
        private Integer rank;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShiftSummary {
        private Long shiftId;
        private String employeeName;
        private Long employeeId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String status;
        private Long durationMinutes;
        private BigDecimal totalSales;
        private Long totalOrders;
        private BigDecimal startingCash;
        private BigDecimal endingCash;
        private BigDecimal cashDifference;
    }

    // ============== PROFIT REPORT DTOs ==============

    /**
     * Comprehensive profit report for a date range
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfitReport {
        private LocalDate startDate;
        private LocalDate endDate;
        private String storeName;
        private Long storeId;
        
        // Summary metrics
        private BigDecimal totalRevenue;        // Total sales (after discounts)
        private BigDecimal totalCost;           // Total cost of goods sold
        private BigDecimal grossProfit;         // Revenue - Cost
        private BigDecimal totalDiscounts;      // Total discounts given
        private BigDecimal netProfit;           // Gross profit - Discounts (already factored in revenue)
        private BigDecimal profitMargin;        // (Net Profit / Revenue) * 100
        
        // Order counts
        private Long totalOrders;
        private Long totalItemsSold;
        
        // Breakdown by product
        private List<ProductProfit> productProfits;
        
        // Breakdown by category
        private List<CategoryProfit> categoryProfits;
        
        // Daily profit trend
        private List<DailyProfit> dailyProfits;
        
        // Top profitable products
        private List<ProductProfit> topProfitableProducts;
        
        // Least profitable products (potential concern)
        private List<ProductProfit> leastProfitableProducts;
    }

    /**
     * Profit breakdown for a single product
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductProfit {
        private Long productId;
        private String productName;
        private String sku;
        private String category;
        
        private Long quantitySold;
        private BigDecimal avgSellingPrice;
        private BigDecimal avgCostPrice;
        
        private BigDecimal totalRevenue;
        private BigDecimal totalCost;
        private BigDecimal totalDiscount;
        private BigDecimal grossProfit;
        private BigDecimal netProfit;           // Profit after discounts
        private BigDecimal profitMargin;        // Percentage
        private BigDecimal profitPerUnit;
        
        private Integer rank;                   // Rank by profit
    }

    /**
     * Profit breakdown by category
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryProfit {
        private String category;
        private Long productCount;
        private Long quantitySold;
        
        private BigDecimal totalRevenue;
        private BigDecimal totalCost;
        private BigDecimal totalDiscount;
        private BigDecimal grossProfit;
        private BigDecimal netProfit;
        private BigDecimal profitMargin;
        
        private BigDecimal percentageOfTotalProfit;
    }

    /**
     * Daily profit for trend analysis
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyProfit {
        private LocalDate date;
        private Long orderCount;
        private Long itemsSold;
        
        private BigDecimal revenue;
        private BigDecimal cost;
        private BigDecimal discount;
        private BigDecimal grossProfit;
        private BigDecimal netProfit;
        private BigDecimal profitMargin;
    }
}

