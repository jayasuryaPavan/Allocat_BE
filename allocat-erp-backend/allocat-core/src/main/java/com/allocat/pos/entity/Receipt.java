package com.allocat.pos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a receipt for a sales order
 */
@Entity
@Table(name = "receipts", indexes = {
        @Index(name = "idx_receipts_receipt_no", columnList = "receipt_no"),
        @Index(name = "idx_receipts_sales_order_id", columnList = "sales_order_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receipt_no", unique = true, nullable = false, length = 50)
    private String receiptNo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id", nullable = false)
    private SalesOrder salesOrder;

    @Column(name = "format", length = 20)
    @Builder.Default
    private String format = "PDF";

    @Column(name = "sent_to", length = 100)
    private String sentTo;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
