package com.allocat.api.dto.stocktransfer;

import com.allocat.inventory.entity.StockTransferItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransferItemResponse {
    private Long id;
    private Long productId;
    private String productCode;
    private String productName;
    private Integer quantity;
    private Integer receivedQuantity;
    private Integer damagedQuantity;

    public static StockTransferItemResponse fromEntity(StockTransferItem item) {
        StockTransferItemResponse response = StockTransferItemResponse.builder()
                .id(item.getId())
                .quantity(item.getQuantity())
                .receivedQuantity(item.getReceivedQuantity())
                .damagedQuantity(item.getDamagedQuantity())
                .build();

        if (item.getProduct() != null) {
            response.setProductId(item.getProduct().getId());
            response.setProductCode(item.getProduct().getProductCode());
            response.setProductName(item.getProduct().getName());
        }

        return response;
    }
}
