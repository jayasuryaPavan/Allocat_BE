package com.allocat.auth.repository;

import com.allocat.auth.entity.UserStoreAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserStoreAccessRepository extends JpaRepository<UserStoreAccess, Long> {
    
    List<UserStoreAccess> findByUserId(Long userId);
    
    List<UserStoreAccess> findByUserIdAndIsActive(Long userId, Boolean isActive);
    
    List<UserStoreAccess> findByStoreId(Long storeId);
    
    List<UserStoreAccess> findByStoreIdAndIsActive(Long storeId, Boolean isActive);
    
    Optional<UserStoreAccess> findByUserIdAndStoreId(Long userId, Long storeId);
    
    Optional<UserStoreAccess> findByUserIdAndStoreIdAndWarehouseId(Long userId, Long storeId, Long warehouseId);
    
    @Query("SELECT usa FROM UserStoreAccess usa WHERE usa.user.id = :userId " +
           "AND usa.isActive = true " +
           "AND (usa.expiresAt IS NULL OR usa.expiresAt > :now)")
    List<UserStoreAccess> findActiveAccessByUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    @Query("SELECT usa FROM UserStoreAccess usa WHERE usa.user.id = :userId " +
           "AND usa.store.id = :storeId " +
           "AND usa.isActive = true " +
           "AND (usa.expiresAt IS NULL OR usa.expiresAt > :now)")
    List<UserStoreAccess> findActiveAccessByUserAndStore(@Param("userId") Long userId, 
                                                           @Param("storeId") Long storeId,
                                                           @Param("now") LocalDateTime now);
    
    @Query("SELECT usa FROM UserStoreAccess usa WHERE usa.user.id = :userId " +
           "AND usa.isPrimary = true " +
           "AND usa.isActive = true")
    Optional<UserStoreAccess> findPrimaryStoreAccess(@Param("userId") Long userId);
}
