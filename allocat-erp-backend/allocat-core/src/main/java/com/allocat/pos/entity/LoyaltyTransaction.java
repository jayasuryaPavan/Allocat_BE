package com.allocat.pos.entity;

import com.allocat.common.entity.BaseEntity;
import com.allocat.auth.entity.Customer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for loyalty point transactions
 */
@Entity
@Table(name = "loyalty_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LoyaltyTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private SalesOrder order;

    @Column(name = "points_earned", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal pointsEarned = BigDecimal.ZERO;

    @Column(name = "points_redeemed", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal pointsRedeemed = BigDecimal.ZERO;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "description")
    private String description;

    @Column(name = "balance_after", precision = 10, scale = 2)
    private BigDecimal balanceAfter;
}
