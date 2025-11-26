package com.allocat.pos.repository;

import com.allocat.pos.entity.Payment;
import com.allocat.pos.enums.PaymentStatus;
import com.allocat.pos.enums.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Repository for Payment entity
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find all payments for a specific sales order
     */
    List<Payment> findBySalesOrderId(Long salesOrderId);

    /**
     * Find payments by payment type and date range
     */
    List<Payment> findByPaymentTypeAndProcessedAtBetween(
            PaymentType paymentType,
            LocalDateTime startDate,
            LocalDateTime endDate);

    /**
     * Find payments by status
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * Get total payment amount by store and date range
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
            "WHERE p.salesOrder.store.id = :storeId " +
            "AND p.processedAt BETWEEN :startDate AND :endDate " +
            "AND p.status = 'COMPLETED'")
    BigDecimal getTotalPaymentsByStoreAndDateRange(
            @Param("storeId") Long storeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get payment breakdown by payment type
     */
    @Query("SELECT p.paymentType, SUM(p.amount) FROM Payment p " +
            "WHERE p.salesOrder.store.id = :storeId " +
            "AND p.processedAt BETWEEN :startDate AND :endDate " +
            "AND p.status = 'COMPLETED' " +
            "GROUP BY p.paymentType")
    List<Object[]> getPaymentBreakdownByType(
            @Param("storeId") Long storeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get failed payments for a date range
     */
    @Query("SELECT p FROM Payment p " +
            "WHERE p.salesOrder.store.id = :storeId " +
            "AND p.processedAt BETWEEN :startDate AND :endDate " +
            "AND p.status = 'FAILED'")
    List<Payment> getFailedPayments(
            @Param("storeId") Long storeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
