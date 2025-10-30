package com.allocat.inventory.repository;

import com.allocat.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);

    Optional<Inventory> findByProductIdAndLocation(Long productId, String location);

    List<Inventory> findByLocation(String location);

    List<Inventory> findByWarehouse(String warehouse);

    List<Inventory> findByCurrentQuantityGreaterThan(Integer quantity);

    List<Inventory> findByCurrentQuantityLessThan(Integer quantity);

    List<Inventory> findByAvailableQuantityGreaterThan(Integer quantity);

    List<Inventory> findByAvailableQuantityLessThan(Integer quantity);

    @Query("SELECT i FROM Inventory i WHERE i.product.productCode = :productCode")
    Optional<Inventory> findByProductCode(@Param("productCode") String productCode);

    @Query("SELECT i FROM Inventory i WHERE i.product.name LIKE %:productName%")
    List<Inventory> findByProductNameContaining(@Param("productName") String productName);

    @Query("SELECT i FROM Inventory i WHERE i.currentQuantity <= i.product.minimumStockLevel")
    List<Inventory> findLowStockItems();

    @Query("SELECT i FROM Inventory i WHERE i.currentQuantity >= i.product.maximumStockLevel")
    List<Inventory> findOverstockItems();

    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity > 0")
    List<Inventory> findAvailableItems();

    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity = 0")
    List<Inventory> findOutOfStockItems();

    @Query("SELECT i FROM Inventory i WHERE i.batchNumber = :batchNumber")
    List<Inventory> findByBatchNumber(@Param("batchNumber") String batchNumber);

    @Query("SELECT i FROM Inventory i WHERE i.supplierName = :supplierName")
    List<Inventory> findBySupplierName(@Param("supplierName") String supplierName);

    @Query("SELECT i FROM Inventory i WHERE i.expiryDate <= :expiryDate")
    List<Inventory> findExpiringItems(@Param("expiryDate") java.time.LocalDateTime expiryDate);

    @Query("SELECT SUM(i.currentQuantity) FROM Inventory i WHERE i.product.id = :productId")
    Integer getTotalQuantityByProductId(@Param("productId") Long productId);

    @Query("SELECT SUM(i.availableQuantity) FROM Inventory i WHERE i.product.id = :productId")
    Integer getTotalAvailableQuantityByProductId(@Param("productId") Long productId);

    @Query("SELECT SUM(i.reservedQuantity) FROM Inventory i WHERE i.product.id = :productId")
    Integer getTotalReservedQuantityByProductId(@Param("productId") Long productId);
}

