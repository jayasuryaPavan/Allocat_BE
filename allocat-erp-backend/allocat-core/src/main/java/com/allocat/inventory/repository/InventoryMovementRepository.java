package com.allocat.inventory.repository;

import com.allocat.inventory.entity.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    List<InventoryMovement> findByProductIdOrderByMovedAtDesc(Long productId);

    List<InventoryMovement> findByMovedAtBetween(LocalDateTime start, LocalDateTime end);

    List<InventoryMovement> findByMovedBy(Long userId);
}
