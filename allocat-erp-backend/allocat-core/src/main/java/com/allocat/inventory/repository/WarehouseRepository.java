package com.allocat.inventory.repository;

import com.allocat.inventory.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    
    Optional<Warehouse> findByCode(String code);
    
    List<Warehouse> findByStoreId(Long storeId);
    
    List<Warehouse> findByStoreIdAndIsActive(Long storeId, Boolean isActive);
    
    List<Warehouse> findByIsActive(Boolean isActive);
    
    @Query("SELECT w FROM Warehouse w WHERE w.store.id = :storeId AND w.isActive = true")
    List<Warehouse> findActiveWarehousesByStore(@Param("storeId") Long storeId);
    
    boolean existsByCode(String code);
}
