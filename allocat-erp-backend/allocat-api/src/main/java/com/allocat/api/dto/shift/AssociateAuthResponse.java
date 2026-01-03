/*
 * Copyright (c) 2024 Allocat. All rights reserved.
 */

package com.allocat.api.dto.shift;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssociateAuthResponse {

    private Long userId;
    private String associateNumber;
    private String name;
    private Long shiftId;
}
