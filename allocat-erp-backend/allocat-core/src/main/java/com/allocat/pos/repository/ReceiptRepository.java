package com.allocat.pos.repository;

import com.allocat.pos.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Receipt entity
 */
@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    /**
     * Find a receipt by its receipt number
     */
    Optional<Receipt> findByReceiptNo(String receiptNo);

    /**
     * Find a receipt for a specific sales order
     */
    Optional<Receipt> findBySalesOrderId(Long salesOrderId);
}
