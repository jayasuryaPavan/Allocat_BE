package com.allocat.api.dto.store;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreAccessRequest {
    
    @Schema(example = "1", description = "Store ID")
    private Long storeId;
    
    @Schema(example = "ST001", description = "Store code (alternative to storeId)")
    private String storeCode;
    
    @Schema(example = "ACCESS2024!", description = "Access code for store verification", required = true)
    @NotBlank(message = "Access code is required")
    private String accessCode;
}

