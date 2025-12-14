package com.allocat.api.dto.pos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for checkout operation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    private String cartId;
    private Long customerId; // Optional - for walk-in customers
    private List<PaymentRequest> payments;
    private String notes;
    private Boolean emailReceipt;
    private String receiptEmail;
}
