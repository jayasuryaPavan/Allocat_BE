package com.allocat.inventory.repository;

import com.allocat.inventory.entity.StockTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {
    
    Optional<StockTransfer> findByTransferNo(String transferNo);
    
    List<StockTransfer> findByFromStoreId(Long fromStoreId);
    
    List<StockTransfer> findByToStoreId(Long toStoreId);
    
    List<StockTransfer> findByStatus(StockTransfer.TransferStatus status);
    
    List<StockTransfer> findByFromStoreIdAndStatus(Long fromStoreId, StockTransfer.TransferStatus status);
    
    List<StockTransfer> findByToStoreIdAndStatus(Long toStoreId, StockTransfer.TransferStatus status);
    
    @Query("SELECT st FROM StockTransfer st WHERE " +
           "(st.fromStore.id = :storeId OR st.toStore.id = :storeId) " +
           "AND st.status = :status")
    List<StockTransfer> findByStoreIdAndStatus(@Param("storeId") Long storeId, 
                                               @Param("status") StockTransfer.TransferStatus status);
    
    @Query("SELECT st FROM StockTransfer st WHERE " +
           "(st.fromStore.id = :storeId OR st.toStore.id = :storeId) " +
           "AND st.transferDate BETWEEN :startDate AND :endDate")
    List<StockTransfer> findByStoreIdAndDateRange(@Param("storeId") Long storeId,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT st FROM StockTransfer st WHERE " +
           "st.fromWarehouse.id = :warehouseId OR st.toWarehouse.id = :warehouseId")
    List<StockTransfer> findByWarehouseId(@Param("warehouseId") Long warehouseId);
}
