package com.allocat.api.dto.stocktransfer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStockTransferRequest {
    @NotNull(message = "From store ID is required")
    private Long fromStoreId;

    @NotNull(message = "To store ID is required")
    private Long toStoreId;

    private Long fromWarehouseId;
    private Long toWarehouseId;
    private Long fromLocationId;
    private Long toLocationId;
    private String priority; // LOW, NORMAL, HIGH, URGENT
    private String notes;
    private LocalDateTime estimatedDeliveryDate;
    private String shippingMethod;

    @NotEmpty(message = "Transfer items are required")
    @Valid
    private List<TransferItemRequest> items;
}
