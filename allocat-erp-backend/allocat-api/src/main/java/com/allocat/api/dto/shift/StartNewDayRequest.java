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
@Schema(description = "Request to start a new day")
public class StartNewDayRequest {

    @NotNull(message = "Store ID is required")
    @Schema(description = "Store ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long storeId;

    @Schema(description = "Date for the new day (defaults to today)", example = "2024-01-15")
    private LocalDate date;

    @Schema(description = "Initial cash amount for the day", example = "500.00")
    private java.math.BigDecimal initialCashAmount;
}
