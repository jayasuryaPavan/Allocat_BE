package com.allocat.pos.service;

import com.allocat.pos.entity.Discount;
import com.allocat.pos.enums.DiscountType;
import com.allocat.pos.repository.DiscountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing discounts and promotions
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountService {

    private final DiscountRepository discountRepository;

    /**
     * Create a new discount
     */
    @Transactional
    public Discount createDiscount(Discount discount) {
        // Validate discount code is unique
        if (discountRepository.findByCode(discount.getCode()).isPresent()) {
            throw new RuntimeException("Discount code already exists: " + discount.getCode());
        }

        // Validate discount values
        validateDiscount(discount);

        Discount saved = discountRepository.save(discount);
        log.info("Created discount: {}", discount.getCode());
        return saved;
    }

    /**
     * Update an existing discount
     */
    @Transactional
    public Discount updateDiscount(long id, Discount discountDetails) {
        Discount existingDiscount = discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount not found: " + id));

        // Check if code is being changed and if new code already exists
        if (!existingDiscount.getCode().equals(discountDetails.getCode())) {
            if (discountRepository.findByCode(discountDetails.getCode()).isPresent()) {
                throw new RuntimeException("Discount code already exists: " + discountDetails.getCode());
            }
        }

        // Validate updated values
        validateDiscount(discountDetails);

        // Update fields
        existingDiscount.setCode(discountDetails.getCode());
        existingDiscount.setName(discountDetails.getName());
        existingDiscount.setType(discountDetails.getType());
        existingDiscount.setValue(discountDetails.getValue());
        existingDiscount.setMinPurchaseAmount(discountDetails.getMinPurchaseAmount());
        existingDiscount.setMaxDiscountAmount(discountDetails.getMaxDiscountAmount());
        existingDiscount.setValidFrom(discountDetails.getValidFrom());
        existingDiscount.setValidTo(discountDetails.getValidTo());
        existingDiscount.setMaxUsageCount(discountDetails.getMaxUsageCount());
        existingDiscount.setIsActive(discountDetails.getIsActive());

        Discount updated = discountRepository.save(existingDiscount);
        log.info("Updated discount: {}", existingDiscount.getCode());
        return updated;
    }

    /**
     * Get discount by code
     */
    public Discount getDiscountByCode(String code) {
        return discountRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Discount not found: " + code));
    }

    /**
     * Get all active discounts
     */
    public List<Discount> getActiveDiscounts() {
        return discountRepository.findActiveDiscountsByDate(LocalDate.now());
    }

    /**
     * Get all discounts (active and inactive)
     */
    public List<Discount> getAllDiscounts() {
        return discountRepository.findAll();
    }

    /**
     * Deactivate a discount
     */
    @Transactional
    public void deactivateDiscount(long id) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount not found: " + id));

        discount.setIsActive(false);
        discountRepository.save(discount);
        log.info("Deactivated discount: {}", discount.getCode());
    }

    /**
     * Deactivate all expired discounts (scheduled task)
     */
    @Transactional
    public int deactivateExpiredDiscounts() {
        int count = discountRepository.deactivateExpiredDiscounts(LocalDate.now());
        if (count > 0) {
            log.info("Deactivated {} expired discounts", count);
        }
        return count;
    }

    /**
     * Validate discount parameters
     */
    private void validateDiscount(Discount discount) {
        if (discount.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Discount value must be greater than zero");
        }

        if (discount.getType() == DiscountType.PERCENTAGE) {
            if (discount.getValue().compareTo(new BigDecimal("100")) > 0) {
                throw new RuntimeException("Percentage discount cannot exceed 100%");
            }
        }

        if (discount.getValidFrom() != null && discount.getValidTo() != null) {
            if (discount.getValidFrom().isAfter(discount.getValidTo())) {
                throw new RuntimeException("Valid from date must be before valid to date");
            }
        }

        if (discount.getMinPurchaseAmount() != null &&
                discount.getMinPurchaseAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Minimum purchase amount cannot be negative");
        }

        if (discount.getMaxDiscountAmount() != null &&
                discount.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Maximum discount amount must be greater than zero");
        }

        if (discount.getMaxUsageCount() != null && discount.getMaxUsageCount() <= 0) {
            throw new RuntimeException("Maximum usage count must be greater than zero");
        }
    }
}
