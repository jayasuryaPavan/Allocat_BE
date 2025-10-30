package com.allocat.auth.repository;

import com.allocat.auth.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    
    Optional<Store> findByCode(String code);
    
    Optional<Store> findByCodeAndAccessCode(String code, String accessCode);
    
    List<Store> findByIsActive(Boolean isActive);
    
    @Query("SELECT s FROM Store s WHERE s.isActive = true ORDER BY s.name ASC")
    List<Store> findAllActiveStores();
    
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Store s WHERE s.code = :code")
    boolean existsByCode(@Param("code") String code);
    
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Store s WHERE s.id = :storeId AND s.accessCode = :accessCode")
    boolean validateAccessCode(@Param("storeId") Long storeId, @Param("accessCode") String accessCode);
}

