package com.allocat.api.controller;

import com.allocat.common.dto.ApiResponse;
import com.allocat.pos.dto.AnalyticsDTO;
import com.allocat.pos.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Analytics and Reporting
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analytics", description = "APIs for sales analytics and business intelligence")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/sales/summary")
    @Operation(summary = "Get sales summary", description = "Get sales summary for a date range")
    public ResponseEntity<ApiResponse<AnalyticsDTO.SalesSummary>> getSalesSummary(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Store ID") @RequestParam Long storeId) {
        try {
            AnalyticsDTO.SalesSummary summary = analyticsService.getSalesSummary(startDate, endDate, storeId);
            return ResponseEntity.ok(ApiResponse.success(summary));
        } catch (Exception e) {
            log.error("Error getting sales summary", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to get sales summary: " + e.getMessage()));
        }
    }

    @GetMapping("/sales/trends")
    @Operation(summary = "Get sales trends", description = "Get sales trends over time (daily/weekly/monthly)")
    public ResponseEntity<ApiResponse<AnalyticsDTO.SalesTrendResponse>> getSalesTrends(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Store ID") @RequestParam Long storeId,
            @Parameter(description = "Period type: daily, weekly, monthly") @RequestParam(defaultValue = "daily") String periodType) {
        try {
            AnalyticsDTO.SalesTrendResponse trends = analyticsService.getSalesTrends(startDate, endDate, storeId,
                    periodType);
            return ResponseEntity.ok(ApiResponse.success(trends));
        } catch (Exception e) {
            log.error("Error getting sales trends", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to get sales trends: " + e.getMessage()));
        }
    }

    @GetMapping("/cashier/performance")
    @Operation(summary = "Get cashier performance", description = "Get performance metrics for all cashiers")
    public ResponseEntity<ApiResponse<List<AnalyticsDTO.CashierPerformance>>> getCashierPerformance(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Store ID") @RequestParam Long storeId) {
        try {
            List<AnalyticsDTO.CashierPerformance> performance = analyticsService.getCashierPerformance(startDate,
                    endDate, storeId);
            return ResponseEntity.ok(ApiResponse.success(performance));
        } catch (Exception e) {
            log.error("Error getting cashier performance", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to get cashier performance: " + e.getMessage()));
        }
    }

    @GetMapping("/products/top-selling")
    @Operation(summary = "Get top selling products", description = "Get top selling products by quantity or revenue")
    public ResponseEntity<ApiResponse<AnalyticsDTO.TopProductsResponse>> getTopSellingProducts(
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Store ID") @RequestParam Long storeId,
            @Parameter(description = "Sort by: quantity or revenue") @RequestParam(defaultValue = "quantity") String sortBy,
            @Parameter(description = "Limit results") @RequestParam(defaultValue = "10") Integer limit) {
        try {
            AnalyticsDTO.TopProductsResponse topProducts = analyticsService.getTopSellingProducts(startDate, endDate,
                    storeId, sortBy, limit);
            return ResponseEntity.ok(ApiResponse.success(topProducts));
        } catch (Exception e) {
            log.error("Error getting top selling products", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to get top selling products: " + e.getMessage()));
        }
    }

    @GetMapping("/products/low-stock")
    @Operation(summary = "Get low stock alerts", description = "Get products with low stock levels")
    public ResponseEntity<ApiResponse<List<AnalyticsDTO.LowStockAlert>>> getLowStockAlerts(
            @Parameter(description = "Store ID") @RequestParam Long storeId,
            @Parameter(description = "Stock threshold") @RequestParam(defaultValue = "10") Integer threshold) {
        try {
            List<AnalyticsDTO.LowStockAlert> alerts = analyticsService.getLowStockAlerts(storeId, threshold);
            return ResponseEntity.ok(ApiResponse.success(alerts));
        } catch (Exception e) {
            log.error("Error getting low stock alerts", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to get low stock alerts: " + e.getMessage()));
        }
    }
}
