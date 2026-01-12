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
public class AssociateAuthRequest {

    @NotNull(message = "Store ID is required")
    private Long storeId;

    @NotBlank(message = "Associate number is required")
    private String associateNumber;

    @NotBlank(message = "Passcode is required")
    private String passcode;
}
