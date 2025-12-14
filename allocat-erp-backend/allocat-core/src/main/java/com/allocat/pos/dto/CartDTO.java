package com.allocat.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing a shopping cart in the POS system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {

    private String cartId;
    private Long storeId;
    private Long cashierId;

    @Builder.Default
    private List<CartItemDTO> items = new ArrayList<>();

    private DiscountDTO discount;

    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    private LocalDateTime createdAt;
}
