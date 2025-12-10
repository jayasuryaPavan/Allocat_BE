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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Sales person login information")
public class SalesPersonLoginResponse {

    @Schema(description = "Login ID", example = "1")
    private Long id;

    @Schema(description = "Store ID", example = "1")
    private Long storeId;

    @Schema(description = "User ID", example = "2")
    private Long userId;

    @Schema(description = "User name", example = "John Doe")
    private String userName;

    @Schema(description = "Shift ID", example = "5")
    private Long shiftId;

    @Schema(description = "Login time", example = "2024-01-15T08:00:00")
    private LocalDateTime loginTime;

    @Schema(description = "Logout time", example = "2024-01-15T16:00:00")
    private LocalDateTime logoutTime;

    @Schema(description = "Login type", example = "SHIFT_START")
    private SalesPersonLogin.LoginType loginType;

    @Schema(description = "Device info", example = "POS Terminal #1")
    private String deviceInfo;

    @Schema(description = "IP address", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "Location", example = "Main Floor")
    private String location;

    @Schema(description = "Created at", example = "2024-01-15T08:00:00")
    private LocalDateTime createdAt;
}
