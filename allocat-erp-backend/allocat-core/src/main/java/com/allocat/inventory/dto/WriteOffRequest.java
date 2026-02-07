package com.allocat.inventory.dto;

import com.allocat.inventory.entity.InventoryMovement;
import com.allocat.inventory.entity.InventoryWriteOff;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WriteOffRequest {
    private Long productId;
    private InventoryMovement.LocationType location;
    private Long aisleId;
    private Integer quantity;
    private InventoryWriteOff.WriteOffReason reason;
    private String description;
}
