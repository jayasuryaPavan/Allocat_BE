package com.allocat.inventory.repository;

import com.allocat.inventory.entity.WarehouseLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseLocationRepository extends JpaRepository<WarehouseLocation, Long> {
    
    Optional<WarehouseLocation> findByWarehouseIdAndCode(Long warehouseId, String code);
    
    List<WarehouseLocation> findByWarehouseId(Long warehouseId);
    
    List<WarehouseLocation> findByWarehouseIdAndIsActive(Long warehouseId, Boolean isActive);
    
    List<WarehouseLocation> findByParentLocationId(Long parentLocationId);
    
    @Query("SELECT wl FROM WarehouseLocation wl WHERE wl.warehouse.id = :warehouseId AND wl.isActive = true")
    List<WarehouseLocation> findActiveLocationsByWarehouse(@Param("warehouseId") Long warehouseId);
}
