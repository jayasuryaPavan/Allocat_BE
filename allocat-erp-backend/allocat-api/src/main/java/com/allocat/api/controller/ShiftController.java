/*
 * Copyright (c) 2024 Allocat. All rights reserved.
 *
 * This software and associated documentation files (the "Software") are the
 * proprietary and confidential information of Allocat. Unauthorized copying,
 * modification, distribution, or use of this Software, via any medium, is
 * strictly prohibited without the express written permission of Allocat.
 */

package com.allocat.api.controller;

import com.allocat.api.dto.shift.*;
import com.allocat.common.dto.ApiResponse;
import com.allocat.pos.entity.SalesPersonLogin;
import com.allocat.pos.entity.Shift;
import com.allocat.pos.entity.ShiftSwap;
import com.allocat.pos.service.ShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shift Management", description = "APIs for managing shifts, shift swaps, and sales person logins")
public class ShiftController {

    private final ShiftService shiftService;

    // ========= Shift Management =========

    @PostMapping("/start")
    @Operation(summary = "Start shift", description = "Start a new shift for a user")
    public ResponseEntity<ApiResponse<ShiftResponse>> startShift(
            @RequestParam Long userId,
            @Valid @RequestBody StartShiftRequest request) {
        try {
            Shift shift = shiftService.startShift(
                    userId,
                    request.getStoreId(),
                    request.getStartingCashAmount(),
                    request.getExpectedStartTime(),
                    request.getExpectedEndTime(),
                    request.getNotes());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(toShiftResponse(shift), "Shift started"));
        } catch (Exception e) {
            log.error("Error starting shift", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{shiftId}/end")
    @Operation(summary = "End shift", description = "End an active shift")
    public ResponseEntity<ApiResponse<ShiftResponse>> endShift(
            @PathVariable Long shiftId,
            @RequestParam Long endedByUserId,
            @Valid @RequestBody EndShiftRequest request) {
        try {
            Shift shift = shiftService.endShift(
                    shiftId,
                    endedByUserId,
                    request.getEndingCashAmount(),
                    request.getExpectedCashAmount(),
                    request.getNotes());
            return ResponseEntity.ok(ApiResponse.success(toShiftResponse(shift), "Shift ended"));
        } catch (Exception e) {
            log.error("Error ending shift", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/active")
    @Operation(summary = "Get active shift", description = "Get active shift for a user in a store")
    public ResponseEntity<ApiResponse<ShiftResponse>> getActiveShift(
            @RequestParam Long storeId,
            @RequestParam Long userId) {
        try {
            Shift shift = shiftService.getActiveShift(storeId, userId);
            return ResponseEntity.ok(ApiResponse.success(toShiftResponse(shift)));
        } catch (Exception e) {
            log.error("Error getting active shift", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{shiftId}")
    @Operation(summary = "Get shift", description = "Get shift by ID")
    public ResponseEntity<ApiResponse<ShiftResponse>> getShift(@PathVariable Long shiftId) {
        try {
            Shift shift = shiftService.getShiftById(shiftId);
            return ResponseEntity.ok(ApiResponse.success(toShiftResponse(shift)));
        } catch (Exception e) {
            log.error("Error getting shift", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/by-date")
    @Operation(summary = "Get shifts by date", description = "List shifts for a store on a specific date")
    public ResponseEntity<ApiResponse<List<ShiftResponse>>> getShiftsByDate(
            @RequestParam Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<ShiftResponse> shifts = shiftService.getShiftsByDate(storeId, date)
                    .stream().map(this::toShiftResponse).collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(shifts));
        } catch (Exception e) {
            log.error("Error getting shifts by date", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/status")
    @Operation(summary = "Get shifts by status", description = "List shifts for a store by status")
    public ResponseEntity<ApiResponse<List<ShiftResponse>>> getShiftsByStatus(
            @RequestParam Long storeId,
            @RequestParam Shift.ShiftStatus status) {
        try {
            List<ShiftResponse> shifts = shiftService.getShiftsByStatus(storeId, status)
                    .stream().map(this::toShiftResponse).collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(shifts));
        } catch (Exception e) {
            log.error("Error getting shifts by status", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/active/list")
    @Operation(summary = "Get active shifts", description = "List active shifts for a store")
    public ResponseEntity<ApiResponse<List<ShiftResponse>>> getActiveShifts(@RequestParam Long storeId) {
        try {
            List<ShiftResponse> shifts = shiftService.getActiveShifts(storeId)
                    .stream().map(this::toShiftResponse).collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(shifts));
        } catch (Exception e) {
            log.error("Error getting active shifts", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/range")
    @Operation(summary = "Get shifts by date range", description = "List shifts for a store within a date range")
    public ResponseEntity<ApiResponse<List<ShiftResponse>>> getShiftsByRange(
            @RequestParam Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<ShiftResponse> shifts = shiftService.getShiftsByDateRange(storeId, startDate, endDate)
                    .stream().map(this::toShiftResponse).collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(shifts));
        } catch (Exception e) {
            log.error("Error getting shifts by range", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ========= Day Management =========

    @PostMapping("/day/start")
    @Operation(summary = "Start new day", description = "Start a new business day (ensures no active shifts)")
    public ResponseEntity<ApiResponse<Void>> startNewDay(@Valid @RequestBody StartNewDayRequest request) {
        try {
            shiftService.startNewDay(request.getStoreId(), request.getDate(), request.getInitialCashAmount());
            return ResponseEntity.ok(ApiResponse.success(null, "New day started"));
        } catch (Exception e) {
            log.error("Error starting new day", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/day/end")
    @Operation(summary = "End day", description = "End the business day (ensures all shifts ended)")
    public ResponseEntity<ApiResponse<Void>> endDay(@Valid @RequestBody EndDayRequest request) {
        try {
            shiftService.endDay(request.getStoreId(), request.getDate(), request.getNotes());
            return ResponseEntity.ok(ApiResponse.success(null, "Day ended"));
        } catch (Exception e) {
            log.error("Error ending day", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ========= Shift Swap =========

    @PostMapping("/swaps")
    @Operation(summary = "Create shift swap", description = "Create a shift swap request")
    public ResponseEntity<ApiResponse<ShiftSwapResponse>> createShiftSwap(
            @RequestParam Long requestedByUserId,
            @Valid @RequestBody CreateShiftSwapRequest request) {
        try {
            ShiftSwap swap = shiftService.createShiftSwap(
                    requestedByUserId,
                    request.getOriginalShiftId(),
                    request.getRequestedToUserId(),
                    request.getOriginalShiftDate(),
                    request.getSwapShiftDate(),
                    request.getReason());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(toShiftSwapResponse(swap), "Shift swap created"));
        } catch (Exception e) {
            log.error("Error creating shift swap", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/swaps/{swapId}/approve")
    @Operation(summary = "Approve shift swap (employee)", description = "Requested employee approves swap")
    public ResponseEntity<ApiResponse<ShiftSwapResponse>> approveSwapByEmployee(
            @PathVariable Long swapId,
            @RequestParam Long userId) {
        try {
            ShiftSwap swap = shiftService.approveShiftSwapByEmployee(swapId, userId);
            return ResponseEntity.ok(ApiResponse.success(toShiftSwapResponse(swap), "Swap approved by employee"));
        } catch (Exception e) {
            log.error("Error approving swap by employee", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/swaps/{swapId}/manager-approve")
    @Operation(summary = "Approve shift swap (manager)", description = "Manager approves swap")
    public ResponseEntity<ApiResponse<ShiftSwapResponse>> approveSwapByManager(
            @PathVariable Long swapId,
            @RequestParam Long managerId,
            @RequestParam(required = false) String managerNotes) {
        try {
            ShiftSwap swap = shiftService.approveShiftSwapByManager(swapId, managerId, managerNotes);
            return ResponseEntity.ok(ApiResponse.success(toShiftSwapResponse(swap), "Swap approved by manager"));
        } catch (Exception e) {
            log.error("Error approving swap by manager", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/swaps/{swapId}/reject")
    @Operation(summary = "Reject shift swap", description = "Reject a shift swap request")
    public ResponseEntity<ApiResponse<ShiftSwapResponse>> rejectSwap(
            @PathVariable Long swapId,
            @RequestParam Long rejectedByUserId,
            @RequestParam(required = false) String reason) {
        try {
            ShiftSwap swap = shiftService.rejectShiftSwap(swapId, rejectedByUserId, reason);
            return ResponseEntity.ok(ApiResponse.success(toShiftSwapResponse(swap), "Swap rejected"));
        } catch (Exception e) {
            log.error("Error rejecting swap", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/swaps/{swapId}/cancel")
    @Operation(summary = "Cancel shift swap", description = "Cancel a shift swap request")
    public ResponseEntity<ApiResponse<ShiftSwapResponse>> cancelSwap(
            @PathVariable Long swapId,
            @RequestParam Long userId) {
        try {
            ShiftSwap swap = shiftService.cancelShiftSwap(swapId, userId);
            return ResponseEntity.ok(ApiResponse.success(toShiftSwapResponse(swap), "Swap cancelled"));
        } catch (Exception e) {
            log.error("Error cancelling swap", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/swaps/pending")
    @Operation(summary = "Pending swap requests", description = "List pending swap requests for a user")
    public ResponseEntity<ApiResponse<List<ShiftSwapResponse>>> getPendingSwaps(@RequestParam Long userId) {
        try {
            List<ShiftSwapResponse> swaps = shiftService.getPendingSwapRequests(userId)
                    .stream().map(this::toShiftSwapResponse).collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(swaps));
        } catch (Exception e) {
            log.error("Error fetching pending swaps", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/swaps/user")
    @Operation(summary = "User swap requests", description = "List swap requests created by a user")
    public ResponseEntity<ApiResponse<List<ShiftSwapResponse>>> getSwapsByUser(@RequestParam Long userId) {
        try {
            List<ShiftSwapResponse> swaps = shiftService.getSwapRequestsByUser(userId)
                    .stream().map(this::toShiftSwapResponse).collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(swaps));
        } catch (Exception e) {
            log.error("Error fetching user swaps", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/swaps/store")
    @Operation(summary = "Store swap requests", description = "List swap requests for a store")
    public ResponseEntity<ApiResponse<List<ShiftSwapResponse>>> getSwapsByStore(@RequestParam Long storeId) {
        try {
            List<ShiftSwapResponse> swaps = shiftService.getSwapRequestsByStore(storeId)
                    .stream().map(this::toShiftSwapResponse).collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(swaps));
        } catch (Exception e) {
            log.error("Error fetching store swaps", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ========= Sales Person Login =========

    @PostMapping("/login")
    @Operation(summary = "Record sales person login", description = "Record login for a sales person")
    public ResponseEntity<ApiResponse<SalesPersonLoginResponse>> salesPersonLogin(
            @RequestParam Long userId,
            @Valid @RequestBody SalesPersonLoginRequest request) {
        try {
            SalesPersonLogin login = shiftService.recordSalesPersonLogin(
                    userId,
                    request.getStoreId(),
                    request.getShiftId(),
                    request.getLoginType(),
                    request.getDeviceInfo(),
                    request.getIpAddress(),
                    request.getLocation());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(toSalesPersonLoginResponse(login), "Login recorded"));
        } catch (Exception e) {
            log.error("Error recording sales person login", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Record sales person logout", description = "Record logout for a sales person")
    public ResponseEntity<ApiResponse<SalesPersonLoginResponse>> salesPersonLogout(@RequestParam Long userId) {
        try {
            SalesPersonLogin login = shiftService.recordSalesPersonLogout(userId);
            return ResponseEntity.ok(ApiResponse.success(toSalesPersonLoginResponse(login), "Logout recorded"));
        } catch (Exception e) {
            log.error("Error recording sales person logout", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/logins/user")
    @Operation(summary = "User login history", description = "Get login history for a user")
    public ResponseEntity<ApiResponse<List<SalesPersonLoginResponse>>> getLoginHistory(
            @RequestParam Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<SalesPersonLoginResponse> logins = shiftService.getLoginHistory(userId, date)
                    .stream().map(this::toSalesPersonLoginResponse).collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(logins));
        } catch (Exception e) {
            log.error("Error fetching login history", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/logins/store")
    @Operation(summary = "Store login history", description = "Get login history for a store")
    public ResponseEntity<ApiResponse<List<SalesPersonLoginResponse>>> getLoginHistoryByStore(
            @RequestParam Long storeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<SalesPersonLoginResponse> logins = shiftService.getLoginHistoryByStore(storeId, date)
                    .stream().map(this::toSalesPersonLoginResponse).collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success(logins));
        } catch (Exception e) {
            log.error("Error fetching store login history", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ========= Mappers =========

    private ShiftResponse toShiftResponse(Shift shift) {
        return ShiftResponse.builder()
                .id(shift.getId())
                .storeId(shift.getStoreId())
                .userId(shift.getUserId())
                .shiftDate(shift.getShiftDate())
                .startedAt(shift.getStartedAt())
                .endedAt(shift.getEndedAt())
                .expectedStartTime(shift.getExpectedStartTime())
                .expectedEndTime(shift.getExpectedEndTime())
                .startingCashAmount(shift.getStartingCashAmount())
                .endingCashAmount(shift.getEndingCashAmount())
                .expectedCashAmount(shift.getExpectedCashAmount())
                .cashDifference(shift.getCashDifference())
                .status(shift.getStatus())
                .notes(shift.getNotes())
                .endedBy(shift.getEndedBy())
                .createdAt(shift.getCreatedAt())
                .build();
    }

    private ShiftSwapResponse toShiftSwapResponse(ShiftSwap swap) {
        return ShiftSwapResponse.builder()
                .id(swap.getId())
                .storeId(swap.getStoreId())
                .originalShiftId(swap.getOriginalShiftId())
                .requestedByUserId(swap.getRequestedByUserId())
                .requestedToUserId(swap.getRequestedToUserId())
                .originalShiftDate(swap.getOriginalShiftDate())
                .swapShiftDate(swap.getSwapShiftDate())
                .status(swap.getStatus())
                .reason(swap.getReason())
                .managerNotes(swap.getManagerNotes())
                .approvedBy(swap.getApprovedBy())
                .rejectedBy(swap.getRejectedBy())
                .approvedAt(swap.getApprovedAt())
                .rejectedAt(swap.getRejectedAt())
                .createdAt(swap.getCreatedAt())
                .build();
    }

    private SalesPersonLoginResponse toSalesPersonLoginResponse(SalesPersonLogin login) {
        return SalesPersonLoginResponse.builder()
                .id(login.getId())
                .storeId(login.getStoreId())
                .userId(login.getUserId())
                .shiftId(login.getShiftId())
                .loginTime(login.getLoginTime())
                .logoutTime(login.getLogoutTime())
                .loginType(login.getLoginType())
                .deviceInfo(login.getDeviceInfo())
                .ipAddress(login.getIpAddress())
                .location(login.getLocation())
                .createdAt(login.getCreatedAt())
                .build();
    }
}
