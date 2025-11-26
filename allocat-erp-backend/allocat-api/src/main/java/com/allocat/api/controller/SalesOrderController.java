package com.allocat.api.controller;

import com.allocat.common.dto.ApiResponse;
import com.allocat.pos.entity.SalesOrder;
import com.allocat.pos.service.SalesOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Sales Order management
 */
@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sales Orders", description = "APIs for managing sales orders and transactions")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @GetMapping
    @Operation(summary = "Get sales orders", description = "Retrieve sales orders with pagination and filtering")
    public ResponseEntity<ApiResponse<Page<SalesOrder>>> getSalesOrders(
            @Parameter(description = "Store ID") @RequestParam Long storeId,
            @Parameter(description = "Start date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "orderDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDirection) {
        try {
            // Default to today if dates not provided
            if (startDate == null) {
                startDate = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            }
            if (endDate == null) {
                endDate = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
            }

            Sort sort = sortDirection.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<SalesOrder> orders = salesOrderService.getOrdersByStoreAndDateRange(
                    storeId, startDate, endDate, pageable);

            return ResponseEntity.ok(ApiResponse.success(orders,
                    "Retrieved " + orders.getNumberOfElements() + " orders"));
        } catch (Exception e) {
            log.error("Error retrieving sales orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving orders: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieve a specific sales order by ID")
    public ResponseEntity<ApiResponse<SalesOrder>> getOrderById(
            @Parameter(description = "Order ID") @PathVariable Long id) {
        try {
            SalesOrder order = salesOrderService.getOrderById(id);
            return ResponseEntity.ok(ApiResponse.success(order));
        } catch (Exception e) {
            log.error("Error retrieving order: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Order not found: " + e.getMessage()));
        }
    }

    @GetMapping("/order-no/{orderNo}")
    @Operation(summary = "Get order by order number", description = "Retrieve a sales order by order number")
    public ResponseEntity<ApiResponse<SalesOrder>> getOrderByOrderNo(
            @Parameter(description = "Order number") @PathVariable String orderNo) {
        try {
            SalesOrder order = salesOrderService.getOrderByOrderNo(orderNo);
            return ResponseEntity.ok(ApiResponse.success(order));
        } catch (Exception e) {
            log.error("Error retrieving order: {}", orderNo, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Order not found: " + e.getMessage()));
        }
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get customer orders", description = "Retrieve all orders for a specific customer")
    public ResponseEntity<ApiResponse<List<SalesOrder>>> getCustomerOrders(
            @Parameter(description = "Customer ID") @PathVariable Long customerId) {
        try {
            List<SalesOrder> orders = salesOrderService.getOrdersByCustomer(customerId);
            return ResponseEntity.ok(ApiResponse.success(orders,
                    "Retrieved " + orders.size() + " orders for customer"));
        } catch (Exception e) {
            log.error("Error retrieving customer orders: {}", customerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving customer orders: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel a sales order and restore inventory")
    public ResponseEntity<ApiResponse<SalesOrder>> cancelOrder(
            @Parameter(description = "Order ID") @PathVariable Long id,
            @Parameter(description = "Cancellation reason") @RequestParam String reason) {
        try {
            SalesOrder cancelledOrder = salesOrderService.cancelOrder(id, reason);
            return ResponseEntity.ok(ApiResponse.success(cancelledOrder, "Order cancelled successfully"));
        } catch (Exception e) {
            log.error("Error cancelling order: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error cancelling order: " + e.getMessage()));
        }
    }

    @GetMapping("/metrics")
    @Operation(summary = "Get sales metrics", description = "Retrieve sales metrics for a store and date range")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSalesMetrics(
            @Parameter(description = "Store ID") @RequestParam Long storeId,
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            BigDecimal totalSales = salesOrderService.getTotalSales(storeId, startDate, endDate);
            Long orderCount = salesOrderService.getOrderCount(storeId, startDate, endDate);

            BigDecimal averageOrderValue = orderCount > 0
                    ? totalSales.divide(BigDecimal.valueOf(orderCount), 2, java.math.RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            Map<String, Object> metrics = Map.of(
                    "totalSales", totalSales,
                    "orderCount", orderCount,
                    "averageOrderValue", averageOrderValue,
                    "startDate", startDate,
                    "endDate", endDate);

            return ResponseEntity.ok(ApiResponse.success(metrics, "Sales metrics retrieved"));
        } catch (Exception e) {
            log.error("Error retrieving sales metrics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving metrics: " + e.getMessage()));
        }
    }
}
