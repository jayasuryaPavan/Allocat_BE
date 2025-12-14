package com.allocat.pos.dto;

import lombok.Data;
import java.util.List;

@Data
public class ReturnRequest {
    private Long originalOrderId;
    private List<ReturnItemDTO> items;
    private String reason;
    private Long storeId;
    private Long cashierId;

    @Data
    public static class ReturnItemDTO {
        private Long productId;
        private Integer quantity;
    }
}
