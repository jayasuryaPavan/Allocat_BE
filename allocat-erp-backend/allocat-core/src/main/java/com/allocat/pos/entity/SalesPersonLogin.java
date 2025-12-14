/*
 * Copyright (c) 2024 Allocat. All rights reserved.
 * 
 * This software and associated documentation files (the "Software") are the
 * proprietary and confidential information of Allocat. Unauthorized copying,
 * modification, distribution, or use of this Software, via any medium, is
 * strictly prohibited without the express written permission of Allocat.
 */

package com.allocat.pos.entity;

import com.allocat.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entity representing sales person login/logout tracking
 */
@Entity
@Table(name = "sales_person_logins")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class SalesPersonLogin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "shift_id")
    private Long shiftId;

    @Column(name = "login_time", nullable = false)
    private java.time.LocalDateTime loginTime;

    @Column(name = "logout_time")
    private java.time.LocalDateTime logoutTime;

    @Column(name = "login_type", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LoginType loginType = LoginType.SHIFT_START;

    @Column(name = "device_info", length = 255)
    private String deviceInfo;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "location", length = 255)
    private String location;

    public enum LoginType {
        SHIFT_START,    // Login to start a shift
        BREAK_RETURN,   // Returning from break
        SHIFT_END,      // Logout at end of shift
        DAY_END         // End of day logout
    }
}
