package com.allocat.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_write_offs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryWriteOff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryMovement.LocationType location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aisle_id")
    private Aisle aisle;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WriteOffReason reason;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "written_off_by", nullable = false)
    private Long writtenOffBy;

    @Column(name = "written_off_at")
    @Builder.Default
    private LocalDateTime writtenOffAt = LocalDateTime.now();

    public enum WriteOffReason {
        DAMAGED, EXPIRED, SEAL_OPENED, ROTTEN, OTHER
    }
}
