package com.allocat.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnToStorageRequest {
    private Long productId;
    private Long aisleId;
    private Integer quantity;
    private String notes;
}
