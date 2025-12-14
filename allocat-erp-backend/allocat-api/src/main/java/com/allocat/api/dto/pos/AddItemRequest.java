package com.allocat.api.dto.pos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for adding item to cart
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddItemRequest {
    private Long productId;
    private String barcode; // Either productId or barcode required
    private Integer quantity;
}
