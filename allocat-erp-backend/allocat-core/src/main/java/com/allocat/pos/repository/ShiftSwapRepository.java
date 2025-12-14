/*
 * Copyright (c) 2024 Allocat. All rights reserved.
 * 
 * This software and associated documentation files (the "Software") are the
 * proprietary and confidential information of Allocat. Unauthorized copying,
 * modification, distribution, or use of this Software, via any medium, is
 * strictly prohibited without the express written permission of Allocat.
 */

package com.allocat.pos.repository;

import com.allocat.pos.entity.ShiftSwap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShiftSwapRepository extends JpaRepository<ShiftSwap, Long> {

    /**
     * Find pending swap requests for a user (requests sent to them)
     */
    List<ShiftSwap> findByRequestedToUserIdAndStatus(Long userId, ShiftSwap.SwapStatus status);

    /**
     * Find swap requests created by a user
     */
    List<ShiftSwap> findByRequestedByUserId(Long userId);

    /**
     * Find swap requests for a store
     */
    List<ShiftSwap> findByStoreId(Long storeId);

    /**
     * Find swap requests by status for a store
     */
    List<ShiftSwap> findByStoreIdAndStatus(Long storeId, ShiftSwap.SwapStatus status);

    /**
     * Check if a swap already exists for the same shift and dates
     */
    @Query("SELECT COUNT(s) > 0 FROM ShiftSwap s WHERE s.originalShiftId = :shiftId " +
           "AND s.originalShiftDate = :originalDate AND s.swapShiftDate = :swapDate " +
           "AND s.status IN ('PENDING', 'APPROVED', 'MANAGER_APPROVED')")
    boolean existsActiveSwapForShift(@Param("shiftId") Long shiftId,
                                     @Param("originalDate") LocalDate originalDate,
                                     @Param("swapDate") LocalDate swapDate);
}
