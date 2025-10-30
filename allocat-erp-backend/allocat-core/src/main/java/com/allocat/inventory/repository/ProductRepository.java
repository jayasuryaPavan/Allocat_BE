package com.allocat.inventory.repository;

import com.allocat.inventory.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductCode(String productCode);

    Optional<Product> findByBarcode(String barcode);

    Optional<Product> findBySku(String sku);

    List<Product> findByIsActiveTrue();

    Page<Product> findByCategory(String category, Pageable pageable);

    Page<Product> findBySupplierName(String supplierName, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Product> findByCategory(String category);

    List<Product> findBySupplierName(String supplierName);

    List<Product> findByNameContainingIgnoreCase(String name);

    @Query("SELECT p FROM Product p WHERE p.name LIKE %:searchTerm% OR p.productCode LIKE %:searchTerm% OR p.barcode LIKE %:searchTerm%")
    List<Product> searchProducts(@Param("searchTerm") String searchTerm);

    @Query("SELECT p FROM Product p WHERE p.minimumStockLevel IS NOT NULL AND p.minimumStockLevel > 0")
    List<Product> findProductsWithMinimumStockLevel();

    boolean existsByProductCode(String productCode);

    boolean existsByBarcode(String barcode);

    boolean existsBySku(String sku);

    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.category IS NOT NULL ORDER BY p.category")
    List<String> findDistinctCategories();
}
