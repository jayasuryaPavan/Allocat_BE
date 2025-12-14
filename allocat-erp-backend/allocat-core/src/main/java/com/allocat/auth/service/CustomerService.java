package com.allocat.auth.service;

import com.allocat.auth.entity.Customer;
import com.allocat.auth.entity.Store;
import com.allocat.auth.repository.CustomerRepository;
import com.allocat.auth.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public Customer createCustomer(Customer customer) {
        // Verify store exists
        Long storeId = customer.getStore() != null ? customer.getStore().getId() : null;
        if (storeId == null) {
            throw new IllegalArgumentException("Store ID must not be null");
        }

        log.info("Creating new customer with code: {} for store ID: {}", customer.getCustomerCode(), storeId);

        // Check if customer code already exists for this store
        if (customerRepository.existsByCustomerCodeAndStoreId(customer.getCustomerCode(), storeId)) {
            throw new IllegalArgumentException(
                    "Customer with code " + customer.getCustomerCode() + " already exists for this store");
        }
        Store store = storeRepository.findById(storeId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Store not found with ID: " + storeId));

        customer.setStore(store);
        Customer savedCustomer = java.util.Objects.requireNonNull(customerRepository.save(customer));
        log.info("Customer created successfully with ID: {}", savedCustomer.getId());
        return savedCustomer;
    }

    @Transactional
    public Customer updateCustomer(long customerId, Customer updatedCustomer) {
        log.info("Updating customer with ID: {}", customerId);

        Customer existingCustomer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + customerId));

        // Check if customer code is being changed and if it conflicts
        if (updatedCustomer.getCustomerCode() != null &&
                !updatedCustomer.getCustomerCode().equals(existingCustomer.getCustomerCode())) {
            if (customerRepository.existsByCustomerCodeAndStoreId(updatedCustomer.getCustomerCode(),
                    existingCustomer.getStore().getId())) {
                throw new IllegalArgumentException(
                        "Customer with code " + updatedCustomer.getCustomerCode() + " already exists for this store");
            }
            existingCustomer.setCustomerCode(updatedCustomer.getCustomerCode());
        }

        // Update fields (only non-null values)
        if (updatedCustomer.getName() != null) {
            existingCustomer.setName(updatedCustomer.getName());
        }
        if (updatedCustomer.getEmail() != null) {
            existingCustomer.setEmail(updatedCustomer.getEmail());
        }
        if (updatedCustomer.getPhone() != null) {
            existingCustomer.setPhone(updatedCustomer.getPhone());
        }
        if (updatedCustomer.getAddress() != null) {
            existingCustomer.setAddress(updatedCustomer.getAddress());
        }
        if (updatedCustomer.getCity() != null) {
            existingCustomer.setCity(updatedCustomer.getCity());
        }
        if (updatedCustomer.getState() != null) {
            existingCustomer.setState(updatedCustomer.getState());
        }
        if (updatedCustomer.getCountry() != null) {
            existingCustomer.setCountry(updatedCustomer.getCountry());
        }
        if (updatedCustomer.getPostalCode() != null) {
            existingCustomer.setPostalCode(updatedCustomer.getPostalCode());
        }
        if (updatedCustomer.getInvoiceNumber() != null) {
            existingCustomer.setInvoiceNumber(updatedCustomer.getInvoiceNumber());
        }
        if (updatedCustomer.getTaxId() != null) {
            existingCustomer.setTaxId(updatedCustomer.getTaxId());
        }
        if (updatedCustomer.getCompanyName() != null) {
            existingCustomer.setCompanyName(updatedCustomer.getCompanyName());
        }
        if (updatedCustomer.getContactPerson() != null) {
            existingCustomer.setContactPerson(updatedCustomer.getContactPerson());
        }
        if (updatedCustomer.getNotes() != null) {
            existingCustomer.setNotes(updatedCustomer.getNotes());
        }
        if (updatedCustomer.getIsActive() != null) {
            existingCustomer.setIsActive(updatedCustomer.getIsActive());
        }

        @SuppressWarnings("null") // Spring Data JPA save() never returns null
        Customer saved = customerRepository.save(existingCustomer);
        log.info("Customer updated successfully: {}", customerId);
        return saved;
    }

    @Transactional
    public void deleteCustomer(long customerId) {
        log.info("Deleting customer with ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + customerId));

        // Soft delete by setting isActive to false
        customer.setIsActive(false);
        customerRepository.save(customer);

        log.info("Customer soft deleted successfully: {}", customerId);
    }

    public Customer getCustomerById(long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + customerId));
    }

    public Customer getCustomerByCode(String customerCode) {
        return customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with code: " + customerCode));
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public List<Customer> getCustomersByStoreId(Long storeId) {
        return customerRepository.findByStoreId(storeId);
    }

    public List<Customer> getActiveCustomersByStoreId(Long storeId) {
        return customerRepository.findByStoreIdAndIsActiveTrue(storeId);
    }

    public List<Customer> getCustomersByInvoiceNumber(String invoiceNumber) {
        return customerRepository.findByInvoiceNumber(invoiceNumber);
    }

    public List<Customer> searchCustomersByStore(Long storeId, String searchTerm) {
        return customerRepository.searchCustomersByStore(storeId, searchTerm);
    }
}
