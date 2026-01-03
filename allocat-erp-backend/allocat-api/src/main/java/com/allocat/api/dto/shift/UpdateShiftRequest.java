/*
 * Copyright (c) 2024 Allocat. All rights reserved.
 */

package com.allocat.api.dto.shift;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a shift")
public class UpdateShiftRequest {

    @Schema(description = "New start time for the shift")
    private LocalDateTime startedAt;

    @Schema(description = "New end time for the shift")
    private LocalDateTime endedAt;
}
