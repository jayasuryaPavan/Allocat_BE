package com.allocat.pos.repository;

import com.allocat.pos.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Discount entity
 */
@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {

    /**
     * Find a discount by its code
     */
    Optional<Discount> findByCode(String code);

    /**
     * Find all active or inactive discounts
     */
    List<Discount> findByIsActive(Boolean isActive);

    /**
     * Find active discounts valid for a specific date
     */
    @Query("SELECT d FROM Discount d WHERE d.isActive = true " +
            "AND (d.validFrom IS NULL OR d.validFrom <= :date) " +
            "AND (d.validTo IS NULL OR d.validTo >= :date) " +
            "AND (d.maxUsageCount IS NULL OR d.currentUsageCount < d.maxUsageCount)")
    List<Discount> findActiveDiscountsByDate(@Param("date") LocalDate date);

    /**
     * Find discounts that have expired
     */
    @Query("SELECT d FROM Discount d WHERE d.isActive = true " +
            "AND d.validTo IS NOT NULL AND d.validTo < :date")
    List<Discount> findExpiredDiscounts(@Param("date") LocalDate date);

    /**
     * Find discounts that have reached their usage limit
     */
    @Query("SELECT d FROM Discount d WHERE d.isActive = true " +
            "AND d.maxUsageCount IS NOT NULL " +
            "AND d.currentUsageCount >= d.maxUsageCount")
    List<Discount> findDiscountsAtUsageLimit();

    /**
     * Increment usage count for a discount
     */
    @Modifying
    @Query("UPDATE Discount d SET d.currentUsageCount = d.currentUsageCount + 1 " +
            "WHERE d.id = :discountId")
    void incrementUsageCount(@Param("discountId") Long discountId);

    /**
     * Deactivate expired discounts (batch operation)
     */
    @Modifying
    @Query("UPDATE Discount d SET d.isActive = false " +
            "WHERE d.isActive = true " +
            "AND d.validTo IS NOT NULL AND d.validTo < :date")
    int deactivateExpiredDiscounts(@Param("date") LocalDate date);
}
