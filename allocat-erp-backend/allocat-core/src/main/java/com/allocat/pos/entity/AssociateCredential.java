/*
 * Copyright (c) 2024 Allocat. All rights reserved.
 */

package com.allocat.pos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for storing associate credentials for POS Kiosk mode.
 * Associates have a separate number and PIN for shift sign-in/sign-out.
 */
@Entity
@Table(name = "associate_credentials", indexes = {
        @Index(name = "idx_associate_credentials_user_id", columnList = "user_id"),
        @Index(name = "idx_associate_credentials_store_id", columnList = "store_id"),
        @Index(name = "idx_associate_credentials_associate_number", columnList = "associate_number")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssociateCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "associate_number", nullable = false, unique = true, length = 20)
    private String associateNumber;

    @Column(name = "passcode_hash", nullable = false)
    private String passcodeHash;

    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
