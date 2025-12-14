package com.allocat.api.dto.stocktransfer;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiveItemRequest {
    @NotNull(message = "Transfer item ID is required")
    private Long transferItemId;

    @NotNull(message = "Received quantity is required")
    @Min(value = 0, message = "Received quantity cannot be negative")
    private Integer receivedQuantity;

    @Min(value = 0, message = "Damaged quantity cannot be negative")
    private Integer damagedQuantity;
}
