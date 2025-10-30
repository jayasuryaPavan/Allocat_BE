package com.allocat.inventory.repository;

import com.allocat.inventory.entity.ReceivedStock;
import com.allocat.inventory.entity.ReceivedStock.ReceivedStockStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReceivedStockRepository extends JpaRepository<ReceivedStock, Long> {

    List<ReceivedStock> findByStatus(ReceivedStockStatus status);

    List<ReceivedStock> findByProductId(Long productId);

    List<ReceivedStock> findByCsvUploadId(String csvUploadId);

    List<ReceivedStock> findBySupplierName(String supplierName);

    List<ReceivedStock> findByBatchNumber(String batchNumber);

    List<ReceivedStock> findByReceivedDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT rs FROM ReceivedStock rs WHERE rs.status = :status AND rs.receivedDate >= :startDate AND rs.receivedDate <= :endDate")
    List<ReceivedStock> findByStatusAndReceivedDateBetween(
            @Param("status") ReceivedStockStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT rs FROM ReceivedStock rs WHERE rs.product.productCode = :productCode")
    List<ReceivedStock> findByProductCode(@Param("productCode") String productCode);

    @Query("SELECT rs FROM ReceivedStock rs WHERE rs.status = 'PENDING' ORDER BY rs.receivedDate ASC")
    List<ReceivedStock> findPendingReceivedStock();

    @Query("SELECT rs FROM ReceivedStock rs WHERE rs.status = 'PENDING' AND rs.csvUploadId = :csvUploadId")
    List<ReceivedStock> findPendingByCsvUploadId(@Param("csvUploadId") String csvUploadId);

    @Query("SELECT COUNT(rs) FROM ReceivedStock rs WHERE rs.status = :status")
    Long countByStatus(@Param("status") ReceivedStockStatus status);

    @Query("SELECT rs FROM ReceivedStock rs WHERE rs.status = 'PENDING' AND rs.expectedQuantity != rs.receivedQuantity")
    List<ReceivedStock> findDiscrepancies();

    Optional<ReceivedStock> findByProductIdAndBatchNumber(Long productId, String batchNumber);
}

