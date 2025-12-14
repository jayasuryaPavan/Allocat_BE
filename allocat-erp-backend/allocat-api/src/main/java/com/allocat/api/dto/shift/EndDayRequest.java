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
import lombok.Data;

@Data
@Schema(description = "Request to end the day")
public class EndDayRequest {

    @Schema(description = "Store ID", example = "1")
    private Long storeId;

    @Schema(description = "Date to end (defaults to today)", example = "2024-01-15")
    private java.time.LocalDate date;

    @Schema(description = "Notes about the day end", example = "All shifts completed")
    private String notes;
}
