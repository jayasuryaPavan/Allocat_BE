/*
 * Copyright (c) 2024 Allocat. All rights reserved.
 * 
 * This software and associated documentation files (the "Software") are the
 * proprietary and confidential information of Allocat. Unauthorized copying,
 * modification, distribution, or use of this Software, via any medium, is
 * strictly prohibited without the express written permission of Allocat.
 */

package com.allocat.pos.repository;

import com.allocat.pos.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {

    /**
     * Find active shift for a user in a store
     */
    @Query("SELECT s FROM Shift s WHERE s.storeId = :storeId AND s.userId = :userId " +
           "AND s.status = 'ACTIVE' AND s.startedAt IS NOT NULL AND s.endedAt IS NULL")
    Optional<Shift> findActiveShiftByStoreAndUser(@Param("storeId") Long storeId, @Param("userId") Long userId);

    /**
     * Find shifts for a user on a specific date
     */
    List<Shift> findByStoreIdAndUserIdAndShiftDate(Long storeId, Long userId, LocalDate shiftDate);

    /**
     * Find all shifts for a store on a specific date
     */
    List<Shift> findByStoreIdAndShiftDate(Long storeId, LocalDate shiftDate);

    /**
     * Find shifts by status
     */
    List<Shift> findByStoreIdAndStatus(Long storeId, Shift.ShiftStatus status);

    /**
     * Find active shifts for a store
     */
    @Query("SELECT s FROM Shift s WHERE s.storeId = :storeId AND s.status = 'ACTIVE'")
    List<Shift> findActiveShiftsByStore(@Param("storeId") Long storeId);

    /**
     * Find shifts within date range
     */
    @Query("SELECT s FROM Shift s WHERE s.storeId = :storeId AND s.shiftDate >= :startDate AND s.shiftDate <= :endDate")
    List<Shift> findShiftsByDateRange(@Param("storeId") Long storeId, 
                                      @Param("startDate") LocalDate startDate, 
                                      @Param("endDate") LocalDate endDate);

    /**
     * Check if user has an active shift
     */
    @Query("SELECT COUNT(s) > 0 FROM Shift s WHERE s.userId = :userId AND s.status = 'ACTIVE'")
    boolean hasActiveShift(@Param("userId") Long userId);
}
