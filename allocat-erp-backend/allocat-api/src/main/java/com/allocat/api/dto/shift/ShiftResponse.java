/*
 * Copyright (c) 2024 Allocat. All rights reserved.
 * 
 * This software and associated documentation files (the "Software") are the
 * proprietary and confidential information of Allocat. Unauthorized copying,
 * modification, distribution, or use of this Software, via any medium, is
 * strictly prohibited without the express written permission of Allocat.
 */

package com.allocat.api.dto.shift;

import com.allocat.pos.entity.Shift;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Shift information")
public class ShiftResponse {

    @Schema(description = "Shift ID", example = "1")
    private Long id;

    @Schema(description = "Store ID", example = "1")
    private Long storeId;

    @Schema(description = "User ID", example = "2")
    private Long userId;

    @Schema(description = "User name", example = "John Doe")
    private String userName;

    @Schema(description = "Shift date", example = "2024-01-15")
    private LocalDate shiftDate;

    @Schema(description = "When shift started", example = "2024-01-15T08:00:00")
    private LocalDateTime startedAt;

    @Schema(description = "When shift ended", example = "2024-01-15T16:00:00")
    private LocalDateTime endedAt;

    @Schema(description = "Expected start time", example = "2024-01-15T08:00:00")
    private LocalDateTime expectedStartTime;

    @Schema(description = "Expected end time", example = "2024-01-15T16:00:00")
    private LocalDateTime expectedEndTime;

    @Schema(description = "Starting cash amount", example = "500.00")
    private BigDecimal startingCashAmount;

    @Schema(description = "Ending cash amount", example = "1523.50")
    private BigDecimal endingCashAmount;

    @Schema(description = "Expected cash amount", example = "1520.00")
    private BigDecimal expectedCashAmount;

    @Schema(description = "Cash difference", example = "3.50")
    private BigDecimal cashDifference;

    @Schema(description = "Shift status", example = "ACTIVE")
    private Shift.ShiftStatus status;

    @Schema(description = "Notes", example = "Morning shift")
    private String notes;

    @Schema(description = "Ended by user ID", example = "2")
    private Long endedBy;

    @Schema(description = "Created at", example = "2024-01-15T07:55:00")
    private LocalDateTime createdAt;
}
