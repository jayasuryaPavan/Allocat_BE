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

@Data
@Schema(description = "Request to end a shift")
public class EndShiftRequest {

    @NotNull(message = "Ending cash amount is required")
    @Schema(description = "Ending cash amount in the register", example = "1523.50", required = true)
    private BigDecimal endingCashAmount;

    @Schema(description = "Expected cash amount", example = "1520.00")
    private BigDecimal expectedCashAmount;

    @Schema(description = "Notes about the shift ending", example = "All transactions completed")
    private String notes;
}
