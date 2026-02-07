package com.allocat.inventory.repository;

import com.allocat.inventory.entity.InventoryWriteOff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryWriteOffRepository extends JpaRepository<InventoryWriteOff, Long> {
    List<InventoryWriteOff> findByProductIdOrderByWrittenOffAtDesc(Long productId);

    List<InventoryWriteOff> findByReason(InventoryWriteOff.WriteOffReason reason);

    List<InventoryWriteOff> findByWrittenOffAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT w.reason, SUM(w.quantity) FROM InventoryWriteOff w GROUP BY w.reason")
    List<Object[]> sumQuantityByReasonGrouped();
}
