package com.allocat.api.dto.store;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreContextResponse {
    
    @Schema(description = "Currently selected store")
    private StoreResponse store;
    
    @Schema(description = "Whether the access was validated")
    private boolean validated;
    
    @Schema(description = "Message")
    private String message;
}

