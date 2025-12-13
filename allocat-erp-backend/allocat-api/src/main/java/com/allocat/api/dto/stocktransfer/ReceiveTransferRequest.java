package com.allocat.api.dto.stocktransfer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiveTransferRequest {
    @NotEmpty(message = "Received items are required")
    @Valid
    private List<ReceiveItemRequest> receivedItems;
}
