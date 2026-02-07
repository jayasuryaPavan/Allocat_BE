package com.allocat.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AisleRequest {
    private String aisleNumber;
    private String aisleName;
    private String productType;
    private String description;
    private Boolean isActive;
}
