package com.allocat.pos.entity;

import com.allocat.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entity for Loyalty Program configuration
 */
@Entity
@Table(name = "loyalty_programs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LoyaltyProgram extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "points_per_dollar", precision = 10, scale = 2, nullable = false)
    private BigDecimal pointsPerDollar; // e.g., 1.0 = 1 point per dollar spent

    @Column(name = "redemption_rate", precision = 10, scale = 2, nullable = false)
    private BigDecimal redemptionRate; // e.g., 100 points = $1 discount

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "minimum_purchase", precision = 10, scale = 2)
    private BigDecimal minimumPurchase; // Minimum purchase amount to earn points

    @Column(name = "max_redemption_per_transaction", precision = 10, scale = 2)
    private BigDecimal maxRedemptionPerTransaction; // Max discount from points per transaction
}
