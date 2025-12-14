package com.allocat.pos.entity;

import com.allocat.inventory.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entity representing a line item in a sales order
 */
@Entity
@Table(name = "sales_order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "salesOrder")
public class SalesOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id", nullable = false)
    private SalesOrder salesOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    /**
     * Cost price at the time of sale (from inventory)
     * Used for profit calculation
     */
    @Column(name = "cost_price", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal costPrice = BigDecimal.ZERO;

    @Column(name = "discount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total", precision = 12, scale = 2, nullable = false)
    private BigDecimal total;

    /**
     * Calculate the total for this line item
     */
    public BigDecimal calculateTotal() {
        BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        itemTotal = itemTotal.subtract(discount);
        itemTotal = itemTotal.add(taxAmount);
        this.total = itemTotal;
        return itemTotal;
    }

    /**
     * Calculate profit for this line item
     * Profit = (Selling Price - Cost Price) * Quantity - Discount
     */
    public BigDecimal calculateProfit() {
        if (costPrice == null || costPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO; // Cannot calculate profit without cost price
        }
        BigDecimal profitPerUnit = unitPrice.subtract(costPrice);
        BigDecimal grossProfit = profitPerUnit.multiply(BigDecimal.valueOf(quantity));
        // Subtract discount from profit
        BigDecimal netProfit = grossProfit.subtract(discount != null ? discount : BigDecimal.ZERO);
        return netProfit;
    }

    /**
     * Calculate profit margin percentage
     */
    public BigDecimal calculateProfitMargin() {
        if (costPrice == null || costPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal profit = calculateProfit();
        BigDecimal revenue = total != null ? total : calculateTotal();
        if (revenue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return profit.multiply(BigDecimal.valueOf(100))
                .divide(revenue, 2, java.math.RoundingMode.HALF_UP);
    }
}
