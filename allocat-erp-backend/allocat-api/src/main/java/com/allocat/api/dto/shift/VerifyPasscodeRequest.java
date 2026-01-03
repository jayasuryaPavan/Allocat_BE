/*
 * Copyright (c) 2024 Allocat. All rights reserved.
 */

package com.allocat.api.dto.shift;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPasscodeRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Passcode is required")
    private String passcode;
}
