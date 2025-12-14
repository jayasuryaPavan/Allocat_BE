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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Request to start a new shift")
public class StartShiftRequest {

    @NotNull(message = "Store ID is required")
    @Schema(description = "Store ID where the shift is starting", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long storeId;

    @Schema(description = "Starting cash amount in the register", example = "500.00")
    private BigDecimal startingCashAmount;

    @Schema(description = "Expected start time", example = "2024-01-15T08:00:00")
    private LocalDateTime expectedStartTime;

    @Schema(description = "Expected end time", example = "2024-01-15T16:00:00")
    private LocalDateTime expectedEndTime;

    @Schema(description = "Notes for the shift", example = "Morning shift")
    private String notes;
}
