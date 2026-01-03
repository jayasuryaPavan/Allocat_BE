/*
 * Copyright (c) 2024 Allocat. All rights reserved.
 */

package com.allocat.pos.repository;

import com.allocat.pos.entity.AssociateCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for AssociateCredential entity operations.
 */
@Repository
public interface AssociateCredentialRepository extends JpaRepository<AssociateCredential, Long> {

    /**
     * Find active credential by associate number and store ID
     */
    Optional<AssociateCredential> findByAssociateNumberAndStoreIdAndIsActiveTrue(String associateNumber, Long storeId);

    /**
     * Find active credential by associate number (any store)
     */
    Optional<AssociateCredential> findByAssociateNumberAndIsActiveTrue(String associateNumber);

    /**
     * Find active credential by user ID
     */
    Optional<AssociateCredential> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * Find credential by associate number
     */
    Optional<AssociateCredential> findByAssociateNumber(String associateNumber);

    /**
     * Check if associate number exists
     */
    boolean existsByAssociateNumber(String associateNumber);
}
