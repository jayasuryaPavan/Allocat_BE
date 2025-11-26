package com.allocat.pos.repository;

import com.allocat.pos.entity.SalesOrder;
import com.allocat.pos.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for SalesOrder entity
 */
@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

        /**
         * Find a sales order by its order number
         */
        Optional<SalesOrder> findByOrderNo(String orderNo);

        /**
         * Find sales orders by store and date range
         */
        @Query("SELECT s FROM SalesOrder s WHERE s.store.id = :storeId " +
                        "AND s.orderDate BETWEEN :startDate AND :endDate")
        Page<SalesOrder> findByStoreAndDateRange(
                        @Param("storeId") Long storeId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        /**
         * Find sales orders by customer
         */
        List<SalesOrder> findByCustomerId(Long customerId);

        /**
         * Find sales orders by store and status
         */
        List<SalesOrder> findByStoreIdAndStatus(Long storeId, OrderStatus status);

        /**
         * Find sales orders by cashier
         */
        List<SalesOrder> findByCashierId(Long cashierId);

        /**
         * Get total sales amount by store and date range
         */
        @Query("SELECT COALESCE(SUM(s.total), 0) FROM SalesOrder s " +
                        "WHERE s.store.id = :storeId " +
                        "AND s.orderDate BETWEEN :startDate AND :endDate " +
                        "AND s.status != 'CANCELLED'")
        BigDecimal getTotalSalesByStoreAndDateRange(
                        @Param("storeId") Long storeId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * Get count of orders by store and date range
         */
        @Query("SELECT COUNT(s) FROM SalesOrder s " +
                        "WHERE s.store.id = :storeId " +
                        "AND s.orderDate BETWEEN :startDate AND :endDate " +
                        "AND s.status != 'CANCELLED'")
        Long countOrdersByStoreAndDateRange(
                        @Param("storeId") Long storeId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * Get average order value by store and date range
         */
        @Query("SELECT AVG(s.total) FROM SalesOrder s " +
                        "WHERE s.store.id = :storeId " +
                        "AND s.orderDate BETWEEN :startDate AND :endDate " +
                        "AND s.status != 'CANCELLED'")
        BigDecimal getAverageOrderValue(
                        @Param("storeId") Long storeId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * Find sales orders by date range, store, and status (for analytics)
         */
        List<SalesOrder> findByOrderDateBetweenAndStoreIdAndStatus(
                        LocalDateTime startDate,
                        LocalDateTime endDate,
                        Long storeId,
                        OrderStatus status);
}
