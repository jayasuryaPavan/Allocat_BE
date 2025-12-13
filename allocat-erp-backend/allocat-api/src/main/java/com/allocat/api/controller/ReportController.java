package com.allocat.api.controller;

import com.allocat.common.dto.ApiResponse;
import com.allocat.pos.dto.ReportDTO;
import com.allocat.pos.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST Controller for Shift and Day Reports
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports", description = "APIs for Shift and Day Reports")
public class ReportController {

    private final ReportService reportService;

    /**
     * Get detailed shift report
     * 
     * Returns complete sales data for a single shift including:
     * - Employee name, shift times, duration
     * - Cash reconciliation (starting, ending, difference)
     * - All orders processed during shift
     * - Payment method breakdown
     * - Top products sold
     */
    @GetMapping("/shift/{shiftId}")
    @Operation(
            summary = "Get shift report",
            description = "Get complete sales data for a single shift including cash reconciliation, orders, and payment breakdown"
    )
    @PreAuthorize("hasAnyAuthority('reports:view', 'SUPER_ADMIN', 'ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<ReportDTO.ShiftReport>> getShiftReport(
            @Parameter(description = "Shift ID") @PathVariable Long shiftId) {
        try {
            log.info("Getting shift report for shiftId: {}", shiftId);
            ReportDTO.ShiftReport report = reportService.getShiftReport(shiftId);
            return ResponseEntity.ok(ApiResponse.success(report));
        } catch (RuntimeException e) {
            log.error("Error getting shift report for shiftId: {}", shiftId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get shift report: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error getting shift report for shiftId: {}", shiftId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("An unexpected error occurred: " + e.getMessage()));
        }
    }

    /**
     * Get day report for a store
     * 
     * Returns aggregated data for entire business day including:
     * - All shifts summary
     * - Total daily sales & orders
     * - Hourly sales breakdown
     * - Employee performance rankings
     * - Payment method totals
     */
    @GetMapping("/day")
    @Operation(
            summary = "Get day report",
            description = "Get aggregated data for entire business day including all shifts, hourly breakdown, and employee performance"
    )
    @PreAuthorize("hasAnyAuthority('reports:view', 'SUPER_ADMIN', 'ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<ReportDTO.DayReport>> getDayReport(
            @Parameter(description = "Store ID", required = true) 
            @RequestParam Long storeId,
            @Parameter(description = "Date (yyyy-MM-dd)", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            log.info("Getting day report for storeId: {} on date: {}", storeId, date);
            ReportDTO.DayReport report = reportService.getDayReport(storeId, date);
            return ResponseEntity.ok(ApiResponse.success(report));
        } catch (RuntimeException e) {
            log.error("Error getting day report for storeId: {} on date: {}", storeId, date, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get day report: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error getting day report for storeId: {} on date: {}", storeId, date, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("An unexpected error occurred: " + e.getMessage()));
        }
    }

    /**
     * Get shift report for the current user's active shift
     */
    @GetMapping("/shift/current")
    @Operation(
            summary = "Get current shift report",
            description = "Get report for the current user's active shift"
    )
    public ResponseEntity<ApiResponse<ReportDTO.ShiftReport>> getCurrentShiftReport(
            @Parameter(description = "Store ID", required = true) @RequestParam Long storeId,
            @Parameter(description = "User ID", required = true) @RequestParam Long userId) {
        try {
            log.info("Getting current shift report for userId: {} at storeId: {}", userId, storeId);
            // This will need to first find the active shift for the user
            // For now, we delegate to the service which can be enhanced
            return ResponseEntity.ok(ApiResponse.error("Use GET /api/reports/shift/{shiftId} with the active shift ID"));
        } catch (Exception e) {
            log.error("Error getting current shift report", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("An unexpected error occurred: " + e.getMessage()));
        }
    }

    /**
     * Get day report for today
     */
    @GetMapping("/day/today")
    @Operation(
            summary = "Get today's day report",
            description = "Get day report for today's date"
    )
    @PreAuthorize("hasAnyAuthority('reports:view', 'SUPER_ADMIN', 'ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<ReportDTO.DayReport>> getTodayReport(
            @Parameter(description = "Store ID", required = true) @RequestParam Long storeId) {
        try {
            LocalDate today = LocalDate.now();
            log.info("Getting today's day report for storeId: {}", storeId);
            ReportDTO.DayReport report = reportService.getDayReport(storeId, today);
            return ResponseEntity.ok(ApiResponse.success(report));
        } catch (RuntimeException e) {
            log.error("Error getting today's day report for storeId: {}", storeId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get day report: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error getting today's day report for storeId: {}", storeId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("An unexpected error occurred: " + e.getMessage()));
        }
    }

    // ============== PROFIT REPORTS ==============

    /**
     * Get comprehensive profit report for a date range
     * 
     * Returns profit analysis including:
     * - Total revenue, cost, and profit
     * - Profit margin percentage
     * - Profit breakdown by product
     * - Profit breakdown by category
     * - Daily profit trend
     * - Top and least profitable products
     */
    @GetMapping("/profit")
    @Operation(
            summary = "Get profit report",
            description = "Get comprehensive profit report showing revenue, cost, and profit breakdown for a date range"
    )
    @PreAuthorize("hasAnyAuthority('reports:view', 'SUPER_ADMIN', 'ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<ReportDTO.ProfitReport>> getProfitReport(
            @Parameter(description = "Store ID", required = true) 
            @RequestParam Long storeId,
            @Parameter(description = "Start date (yyyy-MM-dd)", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            log.info("Getting profit report for storeId: {} from {} to {}", storeId, startDate, endDate);
            
            // Validate date range
            if (endDate.isBefore(startDate)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("End date cannot be before start date"));
            }
            
            ReportDTO.ProfitReport report = reportService.getProfitReport(storeId, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success(report));
        } catch (RuntimeException e) {
            log.error("Error getting profit report for storeId: {} from {} to {}", storeId, startDate, endDate, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get profit report: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error getting profit report for storeId: {}", storeId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("An unexpected error occurred: " + e.getMessage()));
        }
    }

    /**
     * Get profit report for the current month
     */
    @GetMapping("/profit/month")
    @Operation(
            summary = "Get monthly profit report",
            description = "Get profit report for the current month"
    )
    @PreAuthorize("hasAnyAuthority('reports:view', 'SUPER_ADMIN', 'ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<ReportDTO.ProfitReport>> getMonthlyProfitReport(
            @Parameter(description = "Store ID", required = true) @RequestParam Long storeId) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate startOfMonth = today.withDayOfMonth(1);
            log.info("Getting monthly profit report for storeId: {} for {}", storeId, today.getMonth());
            
            ReportDTO.ProfitReport report = reportService.getProfitReport(storeId, startOfMonth, today);
            return ResponseEntity.ok(ApiResponse.success(report));
        } catch (RuntimeException e) {
            log.error("Error getting monthly profit report for storeId: {}", storeId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get profit report: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error getting monthly profit report for storeId: {}", storeId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("An unexpected error occurred: " + e.getMessage()));
        }
    }

    /**
     * Get profit report for the current week
     */
    @GetMapping("/profit/week")
    @Operation(
            summary = "Get weekly profit report",
            description = "Get profit report for the current week (last 7 days)"
    )
    @PreAuthorize("hasAnyAuthority('reports:view', 'SUPER_ADMIN', 'ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<ReportDTO.ProfitReport>> getWeeklyProfitReport(
            @Parameter(description = "Store ID", required = true) @RequestParam Long storeId) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate weekAgo = today.minusDays(7);
            log.info("Getting weekly profit report for storeId: {}", storeId);
            
            ReportDTO.ProfitReport report = reportService.getProfitReport(storeId, weekAgo, today);
            return ResponseEntity.ok(ApiResponse.success(report));
        } catch (RuntimeException e) {
            log.error("Error getting weekly profit report for storeId: {}", storeId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get profit report: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error getting weekly profit report for storeId: {}", storeId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("An unexpected error occurred: " + e.getMessage()));
        }
    }

    /**
     * Get profit report for today
     */
    @GetMapping("/profit/today")
    @Operation(
            summary = "Get today's profit report",
            description = "Get profit report for today"
    )
    @PreAuthorize("hasAnyAuthority('reports:view', 'SUPER_ADMIN', 'ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<ReportDTO.ProfitReport>> getTodayProfitReport(
            @Parameter(description = "Store ID", required = true) @RequestParam Long storeId) {
        try {
            LocalDate today = LocalDate.now();
            log.info("Getting today's profit report for storeId: {}", storeId);
            
            ReportDTO.ProfitReport report = reportService.getProfitReport(storeId, today, today);
            return ResponseEntity.ok(ApiResponse.success(report));
        } catch (RuntimeException e) {
            log.error("Error getting today's profit report for storeId: {}", storeId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get profit report: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error getting today's profit report for storeId: {}", storeId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("An unexpected error occurred: " + e.getMessage()));
        }
    }
}

