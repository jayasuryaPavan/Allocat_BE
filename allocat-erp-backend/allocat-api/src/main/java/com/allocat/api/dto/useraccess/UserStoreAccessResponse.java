package com.allocat.api.dto.useraccess;

import com.allocat.auth.entity.UserStoreAccess;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStoreAccessResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long storeId;
    private String storeName;
    private String storeCode;
    private Long warehouseId;
    private String warehouseName;
    private String warehouseCode;
    private String accessLevel;
    private Boolean isPrimary;
    private Long grantedById;
    private String grantedByName;
    private LocalDateTime grantedAt;
    private LocalDateTime expiresAt;
    private Boolean isActive;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserStoreAccessResponse fromEntity(UserStoreAccess access) {
        UserStoreAccessResponse response = UserStoreAccessResponse.builder()
                .id(access.getId())
                .accessLevel(access.getAccessLevel() != null ? access.getAccessLevel().name() : null)
                .isPrimary(access.getIsPrimary())
                .grantedAt(access.getGrantedAt())
                .expiresAt(access.getExpiresAt())
                .isActive(access.getIsActive())
                .notes(access.getNotes())
                .createdAt(access.getCreatedAt())
                .updatedAt(access.getUpdatedAt())
                .build();

        if (access.getUser() != null) {
            response.setUserId(access.getUser().getId());
            response.setUserEmail(access.getUser().getEmail());
            if (access.getUser().getFirstName() != null || access.getUser().getLastName() != null) {
                response.setUserName(
                    (access.getUser().getFirstName() != null ? access.getUser().getFirstName() : "") +
                    " " +
                    (access.getUser().getLastName() != null ? access.getUser().getLastName() : "")
                ).trim();
            }
        }

        if (access.getStore() != null) {
            response.setStoreId(access.getStore().getId());
            response.setStoreName(access.getStore().getName());
            response.setStoreCode(access.getStore().getCode());
        }

        if (access.getWarehouse() != null) {
            response.setWarehouseId(access.getWarehouse().getId());
            response.setWarehouseName(access.getWarehouse().getName());
            response.setWarehouseCode(access.getWarehouse().getCode());
        }

        if (access.getGrantedBy() != null) {
            response.setGrantedById(access.getGrantedBy().getId());
            if (access.getGrantedBy().getFirstName() != null || access.getGrantedBy().getLastName() != null) {
                response.setGrantedByName(
                    (access.getGrantedBy().getFirstName() != null ? access.getGrantedBy().getFirstName() : "") +
                    " " +
                    (access.getGrantedBy().getLastName() != null ? access.getGrantedBy().getLastName() : "")
                ).trim();
            }
        }

        return response;
    }
}
