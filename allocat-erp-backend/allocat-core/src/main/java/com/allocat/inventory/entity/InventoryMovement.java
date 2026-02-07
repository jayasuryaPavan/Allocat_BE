package com.allocat.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_movements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovement {

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
    @Column(name = "from_location", nullable = false)
    private LocationType fromLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_location", nullable = false)
    private LocationType toLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_aisle_id")
    private Aisle fromAisle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_aisle_id")
    private Aisle toAisle;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "moved_by", nullable = false)
    private Long movedBy;

    @Column(name = "moved_at")
    @Builder.Default
    private LocalDateTime movedAt = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum LocationType {
        STORAGE, AISLE
    }
}
