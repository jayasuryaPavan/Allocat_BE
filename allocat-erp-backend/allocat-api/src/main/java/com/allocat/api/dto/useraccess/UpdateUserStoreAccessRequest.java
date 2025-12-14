package com.allocat.api.dto.useraccess;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStoreAccessRequest {
    private String accessLevel;
    private Boolean isPrimary;
    private Boolean isActive;
    private LocalDateTime expiresAt;
    private String notes;
}
