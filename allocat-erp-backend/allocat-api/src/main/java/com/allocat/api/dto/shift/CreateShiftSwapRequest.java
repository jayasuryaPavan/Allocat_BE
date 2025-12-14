/*
 * Copyright (c) 2024 Allocat. All rights reserved.
 * 
 * This software and associated documentation files (the "Software") are the
 * proprietary and confidential information of Allocat. Unauthorized copying,
 * modification, distribution, or use of this Software, via any medium, is
 * strictly prohibited without the express written permission of Allocat.
 */

package com.allocat.api.dto.shift;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Request to create a shift swap")
public class CreateShiftSwapRequest {

    @NotNull(message = "Original shift ID is required")
    @Schema(description = "ID of the shift to swap", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long originalShiftId;

    @NotNull(message = "Requested to user ID is required")
    @Schema(description = "ID of the user to swap with", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long requestedToUserId;

    @NotNull(message = "Original shift date is required")
    @Schema(description = "Date of the original shift", example = "2024-01-20", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate originalShiftDate;

    @NotNull(message = "Swap shift date is required")
    @Schema(description = "Date of the shift to swap to", example = "2024-01-22", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate swapShiftDate;

    @Schema(description = "Reason for the swap request", example = "Family emergency")
    private String reason;
}
