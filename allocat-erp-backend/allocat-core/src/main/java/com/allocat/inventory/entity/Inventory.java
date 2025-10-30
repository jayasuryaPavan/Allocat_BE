package com.allocat.inventory.entity;

import com.allocat.auth.entity.Store;
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
@Table(name = "inventory")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Inventory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Store store;

    @Column(name = "current_quantity", nullable = false)
    private Integer currentQuantity;

    @Column(name = "reserved_quantity")
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Column(name = "unit_cost", precision = 10, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "total_value", precision = 12, scale = 2)
    private BigDecimal totalValue;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "last_updated_by")
    private String lastUpdatedBy;

    @Column(name = "location")
    private String location;

    @Column(name = "warehouse")
    private String warehouse;

    @Column(name = "shelf")
    private String shelf;

    @Column(name = "bin")
    private String bin;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "purchase_order_number")
    private String purchaseOrderNumber;

    @Column(name = "received_stock_id")
    private Long receivedStockId;

    @Column(name = "notes")
    private String notes;

    @PreUpdate
    @PrePersist
    public void calculateAvailableQuantity() {
        this.availableQuantity = this.currentQuantity - this.reservedQuantity;
    }
}

