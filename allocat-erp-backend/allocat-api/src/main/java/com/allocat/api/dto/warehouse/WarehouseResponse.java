package com.allocat.api.dto.warehouse;

import com.allocat.inventory.entity.Warehouse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseResponse {
    private Long id;
    private String code;
    private String name;
    private Long storeId;
    private String storeName;
    private String storeCode;
    private String type;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String phone;
    private String email;
    private Long managerId;
    private String managerName;
    private Boolean isActive;
    private String settings;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static WarehouseResponse fromEntity(Warehouse warehouse) {
        WarehouseResponse response = WarehouseResponse.builder()
                .id(warehouse.getId())
                .code(warehouse.getCode())
                .name(warehouse.getName())
                .type(warehouse.getType() != null ? warehouse.getType().name() : null)
                .address(warehouse.getAddress())
                .city(warehouse.getCity())
                .state(warehouse.getState())
                .country(warehouse.getCountry())
                .postalCode(warehouse.getPostalCode())
                .phone(warehouse.getPhone())
                .email(warehouse.getEmail())
                .isActive(warehouse.getIsActive())
                .settings(warehouse.getSettings())
                .createdAt(warehouse.getCreatedAt())
                .updatedAt(warehouse.getUpdatedAt())
                .build();

        if (warehouse.getStore() != null) {
            response.setStoreId(warehouse.getStore().getId());
            response.setStoreName(warehouse.getStore().getName());
            response.setStoreCode(warehouse.getStore().getCode());
        }

        if (warehouse.getManager() != null) {
            response.setManagerId(warehouse.getManager().getId());
            if (warehouse.getManager().getFirstName() != null || warehouse.getManager().getLastName() != null) {
                String managerName = ((warehouse.getManager().getFirstName() != null
                        ? warehouse.getManager().getFirstName()
                        : "") +
                        " " +
                        (warehouse.getManager().getLastName() != null ? warehouse.getManager().getLastName() : ""))
                        .trim();
                response.setManagerName(managerName);
            }
        }

        return response;
    }
}
