package com.allocat.inventory.repository;

import com.allocat.inventory.entity.StockTransferItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockTransferItemRepository extends JpaRepository<StockTransferItem, Long> {
    
    List<StockTransferItem> findByTransferId(Long transferId);
    
    List<StockTransferItem> findByProductId(Long productId);
}
