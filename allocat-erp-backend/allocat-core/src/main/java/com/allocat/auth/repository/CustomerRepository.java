package com.allocat.auth.repository;

import com.allocat.auth.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByCustomerCode(String customerCode);
    
    List<Customer> findByStoreId(Long storeId);
    
    List<Customer> findByStoreIdAndIsActiveTrue(Long storeId);
    
    List<Customer> findByInvoiceNumber(String invoiceNumber);
    
    List<Customer> findByStoreIdAndInvoiceNumber(Long storeId, String invoiceNumber);
    
    @Query("SELECT c FROM Customer c WHERE c.store.id = :storeId AND (c.name LIKE %:searchTerm% OR c.customerCode LIKE %:searchTerm% OR c.email LIKE %:searchTerm% OR c.invoiceNumber LIKE %:searchTerm%)")
    List<Customer> searchCustomersByStore(@Param("storeId") Long storeId, @Param("searchTerm") String searchTerm);
    
    boolean existsByCustomerCode(String customerCode);
    
    boolean existsByCustomerCodeAndStoreId(String customerCode, Long storeId);
}













