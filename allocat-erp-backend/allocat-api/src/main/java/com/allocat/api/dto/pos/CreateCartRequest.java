package com.allocat.api.dto.pos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new cart
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCartRequest {
    private Long storeId;
    private Long cashierId;
}
