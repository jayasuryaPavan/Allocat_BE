/*
 * Copyright (c) 2024 Allocat. All rights reserved.
 * 
 * This software and associated documentation files (the "Software") are the
 * proprietary and confidential information of Allocat. Unauthorized copying,
 * modification, distribution, or use of this Software, via any medium, is
 * strictly prohibited without the express written permission of Allocat.
 */

package com.allocat.api.dto.shift;

import com.allocat.pos.entity.ShiftSwap;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Shift swap information")
public class ShiftSwapResponse {

    @Schema(description = "Swap ID", example = "1")
    private Long id;

    @Schema(description = "Store ID", example = "1")
    private Long storeId;

    @Schema(description = "Original shift ID", example = "5")
    private Long originalShiftId;

    @Schema(description = "Requested by user ID", example = "2")
    private Long requestedByUserId;

    @Schema(description = "Requested by user name", example = "John Doe")
    private String requestedByName;

    @Schema(description = "Requested to user ID", example = "3")
    private Long requestedToUserId;

    @Schema(description = "Requested to user name", example = "Jane Smith")
    private String requestedToName;

    @Schema(description = "Original shift date", example = "2024-01-20")
    private LocalDate originalShiftDate;

    @Schema(description = "Swap shift date", example = "2024-01-22")
    private LocalDate swapShiftDate;

    @Schema(description = "Swap status", example = "PENDING")
    private ShiftSwap.SwapStatus status;

    @Schema(description = "Reason", example = "Family emergency")
    private String reason;

    @Schema(description = "Manager notes", example = "Approved")
    private String managerNotes;

    @Schema(description = "Approved by user ID", example = "1")
    private Long approvedBy;

    @Schema(description = "Rejected by user ID", example = "1")
    private Long rejectedBy;

    @Schema(description = "Approved at", example = "2024-01-18T10:30:00")
    private LocalDateTime approvedAt;

    @Schema(description = "Rejected at", example = "2024-01-18T10:30:00")
    private LocalDateTime rejectedAt;

    @Schema(description = "Created at", example = "2024-01-18T09:00:00")
    private LocalDateTime createdAt;
}
