package com.allocat.inventory.entity;

import com.allocat.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "received_stock")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ReceivedStock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    @Column(name = "product_code", nullable = false)
    private String productCode;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "expected_quantity", nullable = false)
    private Integer expectedQuantity;

    @Column(name = "received_quantity")
    private Integer receivedQuantity;

    @Column(name = "verified_quantity")
    private Integer verifiedQuantity;

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_value", precision = 12, scale = 2)
    private BigDecimal totalValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ReceivedStockStatus status = ReceivedStockStatus.PENDING;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "supplier_invoice_number")
    private String supplierInvoiceNumber;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @Column(name = "expected_delivery_date")
    private LocalDateTime expectedDeliveryDate;

    @Column(name = "received_date")
    private LocalDateTime receivedDate;

    @Column(name = "verified_date")
    private LocalDateTime verifiedDate;

    @Column(name = "received_by")
    private String receivedBy;

    @Column(name = "verified_by")
    private String verifiedBy;

    @Column(name = "notes")
    private String notes;

    @Column(name = "quality_issues")
    private String qualityIssues;

    @Column(name = "damage_quantity")
    private Integer damageQuantity;

    @Column(name = "shortage_quantity")
    private Integer shortageQuantity;

    @Column(name = "excess_quantity")
    private Integer excessQuantity;

    @Column(name = "csv_upload_id")
    private String csvUploadId;

    @Column(name = "row_number")
    private Integer rowNumber;

    public enum ReceivedStockStatus {
        PENDING,        // Stock received but not verified
        VERIFIED,       // Stock verified and added to inventory
        REJECTED,       // Stock rejected due to quality issues
        PARTIAL,        // Partial verification completed
        DISCREPANCY     // Quantity mismatch found
    }
}

