package com.allocat.api.dto.useraccess;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserStoreAccessRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Store ID is required")
    private Long storeId;

    private Long warehouseId;
    private String accessLevel; // VIEW, OPERATE, MANAGE, ADMIN
    private Boolean isPrimary;
    private LocalDateTime expiresAt;
    private String notes;
}
