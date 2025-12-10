/*
 * Copyright (c) 2024 Allocat. All rights reserved.
 * 
 * This software and associated documentation files (the "Software") are the
 * proprietary and confidential information of Allocat. Unauthorized copying,
 * modification, distribution, or use of this Software, via any medium, is
 * strictly prohibited without the express written permission of Allocat.
 */

package com.allocat.api.dto.shift;

import com.allocat.pos.entity.SalesPersonLogin;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request for sales person login")
public class SalesPersonLoginRequest {

    @NotNull(message = "Store ID is required")
    @Schema(description = "Store ID", example = "1", required = true)
    private Long storeId;

    @Schema(description = "Shift ID if logging in for a specific shift")
    private Long shiftId;

    @Schema(description = "Login type", example = "SHIFT_START")
    private SalesPersonLogin.LoginType loginType;

    @Schema(description = "Device information", example = "POS Terminal #1")
    private String deviceInfo;

    @Schema(description = "IP address", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "Location", example = "Main Floor")
    private String location;
}
