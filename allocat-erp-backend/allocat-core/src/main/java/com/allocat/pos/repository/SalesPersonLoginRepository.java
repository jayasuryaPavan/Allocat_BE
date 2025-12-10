/*
 * Copyright (c) 2024 Allocat. All rights reserved.
 * 
 * This software and associated documentation files (the "Software") are the
 * proprietary and confidential information of Allocat. Unauthorized copying,
 * modification, distribution, or use of this Software, via any medium, is
 * strictly prohibited without the express written permission of Allocat.
 */

package com.allocat.pos.repository;

import com.allocat.pos.entity.SalesPersonLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesPersonLoginRepository extends JpaRepository<SalesPersonLogin, Long> {

    /**
     * Find the latest login for a user
     */
    Optional<SalesPersonLogin> findFirstByUserIdAndLogoutTimeIsNullOrderByLoginTimeDesc(Long userId);

    /**
     * Find all logins for a user on a specific date
     */
    @Query("SELECT l FROM SalesPersonLogin l WHERE l.userId = :userId " +
           "AND DATE(l.loginTime) = :date ORDER BY l.loginTime DESC")
    List<SalesPersonLogin> findLoginsByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * Find all logins for a store on a specific date
     */
    @Query("SELECT l FROM SalesPersonLogin l WHERE l.storeId = :storeId " +
           "AND DATE(l.loginTime) = :date ORDER BY l.loginTime DESC")
    List<SalesPersonLogin> findLoginsByStoreAndDate(@Param("storeId") Long storeId, @Param("date") LocalDate date);

    /**
     * Find active logins (not logged out) for a user
     */
    List<SalesPersonLogin> findByUserIdAndLogoutTimeIsNull(Long userId);

    /**
     * Find logins within date range
     */
    @Query("SELECT l FROM SalesPersonLogin l WHERE l.storeId = :storeId " +
           "AND l.loginTime >= :startDate AND l.loginTime <= :endDate ORDER BY l.loginTime DESC")
    List<SalesPersonLogin> findLoginsByDateRange(@Param("storeId") Long storeId,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    /**
     * Find logins for a specific shift
     */
    List<SalesPersonLogin> findByShiftId(Long shiftId);
}
