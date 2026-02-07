package com.allocat.inventory.repository;

import com.allocat.inventory.entity.Aisle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AisleRepository extends JpaRepository<Aisle, Long> {
    List<Aisle> findByStoreId(Long storeId);

    List<Aisle> findByStoreIdAndIsActiveTrue(Long storeId);

    List<Aisle> findByStoreIdAndProductType(Long storeId, String productType);

    boolean existsByStoreIdAndAisleNumber(Long storeId, String aisleNumber);
}
