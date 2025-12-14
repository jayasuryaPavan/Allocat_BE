package com.allocat.pos.repository;

import com.allocat.pos.entity.SalesOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for SalesOrderItem entity
 */
@Repository
public interface SalesOrderItemRepository extends JpaRepository<SalesOrderItem, Long> {

    /**
     * Find all items for a specific sales order
     */
    List<SalesOrderItem> findBySalesOrderId(Long salesOrderId);

    /**
     * Find all sales involving a specific product
     */
    List<SalesOrderItem> findByProductId(Long productId);

    /**
     * Get top selling products by quantity
     */
    @Query("SELECT soi.product.id, soi.product.name, SUM(soi.quantity) as totalQty " +
            "FROM SalesOrderItem soi " +
            "WHERE soi.salesOrder.store.id = :storeId " +
            "AND soi.salesOrder.orderDate BETWEEN :startDate AND :endDate " +
            "AND soi.salesOrder.status != 'CANCELLED' " +
            "GROUP BY soi.product.id, soi.product.name " +
            "ORDER BY totalQty DESC")
    List<Object[]> getTopSellingProducts(
            @Param("storeId") Long storeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get total quantity sold for a product in a date range
     */
    @Query("SELECT COALESCE(SUM(soi.quantity), 0) FROM SalesOrderItem soi " +
            "WHERE soi.product.id = :productId " +
            "AND soi.salesOrder.store.id = :storeId " +
            "AND soi.salesOrder.orderDate BETWEEN :startDate AND :endDate " +
            "AND soi.salesOrder.status != 'CANCELLED'")
    Integer getTotalQuantitySold(
            @Param("productId") Long productId,
            @Param("storeId") Long storeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
