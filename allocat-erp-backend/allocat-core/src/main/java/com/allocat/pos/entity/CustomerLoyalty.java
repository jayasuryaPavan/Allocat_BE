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
 * Entity linking customers to loyalty programs
 */
@Entity
@Table(name = "customer_loyalty")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CustomerLoyalty extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private LoyaltyProgram program;

    @Column(name = "points_balance", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal pointsBalance = BigDecimal.ZERO;

    @Column(name = "lifetime_points_earned", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal lifetimePointsEarned = BigDecimal.ZERO;

    @Column(name = "tier")
    private String tier; // e.g., "Bronze", "Silver", "Gold", "Platinum"

    @Column(name = "join_date", nullable = false)
    private LocalDateTime joinDate;

    @Column(name = "last_transaction_date")
    private LocalDateTime lastTransactionDate;
}
