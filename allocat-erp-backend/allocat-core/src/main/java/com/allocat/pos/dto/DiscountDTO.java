package com.allocat.pos.dto;

import com.allocat.pos.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for discount information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountDTO {

    private Long id;
    private String code;
    private String name;
    private DiscountType type;
    private BigDecimal value;
    private BigDecimal minPurchaseAmount;
    private BigDecimal maxDiscountAmount;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Integer maxUsageCount;
    private Integer currentUsageCount;
    private Boolean isActive;
}
