package com.allocat.api.controller;

import com.allocat.api.dto.dashboard.ConsolidatedInventoryResponse;
import com.allocat.api.dto.dashboard.StoreInventorySummaryResponse;
import com.allocat.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Consolidated Dashboard", description = "APIs for consolidated inventory dashboards across stores and warehouses")
@SecurityRequirement(name = "Bearer Authentication")
public class DashboardController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/consolidated-inventory")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Get consolidated inventory", 
               description = "Retrieve consolidated inventory across all stores and warehouses")
    public ResponseEntity<ApiResponse<List<ConsolidatedInventoryResponse>>> getConsolidatedInventory(
            @Parameter(description = "Filter by product code or name")
            @RequestParam(required = false) String search) {
        try {
            String sql = "SELECT * FROM v_consolidated_inventory";
            if (search != null && !search.trim().isEmpty()) {
                sql += " WHERE product_code ILIKE ? OR product_name ILIKE ?";
            }
            sql += " ORDER BY product_name";

            List<Map<String, Object>> results;
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search + "%";
                results = jdbcTemplate.queryForList(sql, searchPattern, searchPattern);
            } else {
                results = jdbcTemplate.queryForList(sql);
            }

            List<ConsolidatedInventoryResponse> responses = results.stream()
                    .map(row -> ConsolidatedInventoryResponse.builder()
                            .productId(((Number) row.get("product_id")).longValue())
                            .productCode((String) row.get("product_code"))
                            .productName((String) row.get("product_name"))
                            .sku((String) row.get("sku"))
                            .barcode((String) row.get("barcode"))
                            .totalQuantity(row.get("total_quantity") != null ? 
                                         ((Number) row.get("total_quantity")).intValue() : 0)
                            .totalReserved(row.get("total_reserved") != null ? 
                                         ((Number) row.get("total_reserved")).intValue() : 0)
                            .totalAvailable(row.get("total_available") != null ? 
                                          ((Number) row.get("total_available")).intValue() : 0)
                            .storeCount(row.get("store_count") != null ? 
                                      ((Number) row.get("store_count")).intValue() : 0)
                            .warehouseCount(row.get("warehouse_count") != null ? 
                                          ((Number) row.get("warehouse_count")).intValue() : 0)
                            .minAvailable(row.get("min_available") != null ? 
                                        ((Number) row.get("min_available")).intValue() : 0)
                            .maxAvailable(row.get("max_available") != null ? 
                                        ((Number) row.get("max_available")).intValue() : 0)
                            .avgUnitCost(row.get("avg_unit_cost") != null ? 
                                       (BigDecimal) row.get("avg_unit_cost") : BigDecimal.ZERO)
                            .totalValue(row.get("total_value") != null ? 
                                      (BigDecimal) row.get("total_value") : BigDecimal.ZERO)
                            .build())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(
                    responses,
                    "Consolidated inventory retrieved successfully"
            ));
        } catch (Exception e) {
            log.error("Error retrieving consolidated inventory", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving consolidated inventory: " + e.getMessage()));
        }
    }

    @GetMapping("/store-summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Get store inventory summary", 
               description = "Retrieve inventory summary for all stores")
    public ResponseEntity<ApiResponse<List<StoreInventorySummaryResponse>>> getStoreSummary(
            @Parameter(description = "Filter by store ID")
            @RequestParam(required = false) Long storeId) {
        try {
            String sql = "SELECT * FROM v_store_inventory_summary";
            if (storeId != null) {
                sql += " WHERE store_id = ?";
            }
            sql += " ORDER BY store_name";

            List<Map<String, Object>> results;
            if (storeId != null) {
                results = jdbcTemplate.queryForList(sql, storeId);
            } else {
                results = jdbcTemplate.queryForList(sql);
            }

            List<StoreInventorySummaryResponse> responses = results.stream()
                    .map(row -> StoreInventorySummaryResponse.builder()
                            .storeId(((Number) row.get("store_id")).longValue())
                            .storeCode((String) row.get("store_code"))
                            .storeName((String) row.get("store_name"))
                            .productCount(row.get("product_count") != null ? 
                                        ((Number) row.get("product_count")).intValue() : 0)
                            .totalQuantity(row.get("total_quantity") != null ? 
                                         ((Number) row.get("total_quantity")).intValue() : 0)
                            .totalAvailable(row.get("total_available") != null ? 
                                          ((Number) row.get("total_available")).intValue() : 0)
                            .totalInventoryValue(row.get("total_inventory_value") != null ? 
                                               (BigDecimal) row.get("total_inventory_value") : BigDecimal.ZERO)
                            .warehouseCount(row.get("warehouse_count") != null ? 
                                          ((Number) row.get("warehouse_count")).intValue() : 0)
                            .build())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(
                    responses,
                    "Store inventory summary retrieved successfully"
            ));
        } catch (Exception e) {
            log.error("Error retrieving store summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving store summary: " + e.getMessage()));
        }
    }

    @GetMapping("/warehouse-summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Get warehouse inventory summary", 
               description = "Retrieve inventory summary for all warehouses")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getWarehouseSummary(
            @Parameter(description = "Filter by warehouse ID")
            @RequestParam(required = false) Long warehouseId,
            @Parameter(description = "Filter by store ID")
            @RequestParam(required = false) Long storeId) {
        try {
            String sql = "SELECT * FROM v_warehouse_inventory_summary WHERE 1=1";
            if (warehouseId != null) {
                sql += " AND warehouse_id = ?";
            }
            if (storeId != null) {
                sql += " AND store_id = ?";
            }
            sql += " ORDER BY warehouse_name";

            List<Map<String, Object>> results;
            if (warehouseId != null && storeId != null) {
                results = jdbcTemplate.queryForList(sql, warehouseId, storeId);
            } else if (warehouseId != null) {
                results = jdbcTemplate.queryForList(sql, warehouseId);
            } else if (storeId != null) {
                results = jdbcTemplate.queryForList(sql, storeId);
            } else {
                results = jdbcTemplate.queryForList(sql);
            }

            return ResponseEntity.ok(ApiResponse.success(
                    results,
                    "Warehouse inventory summary retrieved successfully"
            ));
        } catch (Exception e) {
            log.error("Error retrieving warehouse summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving warehouse summary: " + e.getMessage()));
        }
    }
}
