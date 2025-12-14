package com.allocat.api.dto.warehouse;

import com.allocat.inventory.entity.WarehouseLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseLocationResponse {
    private Long id;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private String code;
    private String name;
    private String locationType;
    private Long parentLocationId;
    private String parentLocationCode;
    private Boolean isActive;
    private Integer capacityLimit;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static WarehouseLocationResponse fromEntity(WarehouseLocation location) {
        WarehouseLocationResponse response = WarehouseLocationResponse.builder()
                .id(location.getId())
                .code(location.getCode())
                .name(location.getName())
                .locationType(location.getLocationType() != null ? location.getLocationType().name() : null)
                .isActive(location.getIsActive())
                .capacityLimit(location.getCapacityLimit())
                .notes(location.getNotes())
                .createdAt(location.getCreatedAt())
                .updatedAt(location.getUpdatedAt())
                .build();

        if (location.getWarehouse() != null) {
            response.setWarehouseId(location.getWarehouse().getId());
            response.setWarehouseCode(location.getWarehouse().getCode());
            response.setWarehouseName(location.getWarehouse().getName());
        }

        if (location.getParentLocation() != null) {
            response.setParentLocationId(location.getParentLocation().getId());
            response.setParentLocationCode(location.getParentLocation().getCode());
        }

        return response;
    }
}
