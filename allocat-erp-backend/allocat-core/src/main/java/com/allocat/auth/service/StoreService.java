package com.allocat.auth.service;

import com.allocat.auth.entity.Store;
import com.allocat.auth.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreService {
    
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public Store createStore(Store store) {
        log.info("Creating new store with code: {}", store.getCode());
        
        // Check if store code already exists
        if (storeRepository.existsByCode(store.getCode())) {
            throw new IllegalArgumentException("Store with code " + store.getCode() + " already exists");
        }
        
        // Hash the access code before saving
        String hashedAccessCode = passwordEncoder.encode(store.getAccessCode());
        store.setAccessCode(hashedAccessCode);
        
        Store savedStore = storeRepository.save(store);
        log.info("Store created successfully with ID: {}", savedStore.getId());
        return savedStore;
    }
    
    @Transactional
    public Store updateStore(Long storeId, Store updatedStore, String accessCode) {
        log.info("Updating store with ID: {}", storeId);
        
        Store existingStore = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with ID: " + storeId));
        
        // Validate access code
        if (!validateAccessCode(existingStore, accessCode)) {
            log.warn("Invalid access code provided for store ID: {}", storeId);
            throw new IllegalArgumentException("Invalid access code");
        }
        
        // Update fields (only non-null values)
        if (updatedStore.getName() != null) {
            existingStore.setName(updatedStore.getName());
        }
        if (updatedStore.getAddress() != null) {
            existingStore.setAddress(updatedStore.getAddress());
        }
        if (updatedStore.getCity() != null) {
            existingStore.setCity(updatedStore.getCity());
        }
        if (updatedStore.getState() != null) {
            existingStore.setState(updatedStore.getState());
        }
        if (updatedStore.getCountry() != null) {
            existingStore.setCountry(updatedStore.getCountry());
        }
        if (updatedStore.getPostalCode() != null) {
            existingStore.setPostalCode(updatedStore.getPostalCode());
        }
        if (updatedStore.getPhone() != null) {
            existingStore.setPhone(updatedStore.getPhone());
        }
        if (updatedStore.getEmail() != null) {
            existingStore.setEmail(updatedStore.getEmail());
        }
        if (updatedStore.getTaxId() != null) {
            existingStore.setTaxId(updatedStore.getTaxId());
        }
        if (updatedStore.getCurrency() != null) {
            existingStore.setCurrency(updatedStore.getCurrency());
        }
        if (updatedStore.getTimezone() != null) {
            existingStore.setTimezone(updatedStore.getTimezone());
        }
        if (updatedStore.getIsActive() != null) {
            existingStore.setIsActive(updatedStore.getIsActive());
        }
        
        Store saved = storeRepository.save(existingStore);
        log.info("Store updated successfully: {}", storeId);
        return saved;
    }
    
    @Transactional
    public void updateAccessCode(Long storeId, String oldAccessCode, String newAccessCode) {
        log.info("Updating access code for store ID: {}", storeId);
        
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with ID: " + storeId));
        
        // Validate old access code
        if (!validateAccessCode(store, oldAccessCode)) {
            log.warn("Invalid old access code provided for store ID: {}", storeId);
            throw new IllegalArgumentException("Invalid old access code");
        }
        
        // Hash and set new access code
        String hashedAccessCode = passwordEncoder.encode(newAccessCode);
        store.setAccessCode(hashedAccessCode);
        
        storeRepository.save(store);
        log.info("Access code updated successfully for store ID: {}", storeId);
    }
    
    public Store getStoreById(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with ID: " + storeId));
    }
    
    public Store getStoreByCode(String code) {
        return storeRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with code: " + code));
    }
    
    public List<Store> getAllStores() {
        return storeRepository.findAll();
    }
    
    public List<Store> getAllActiveStores() {
        return storeRepository.findAllActiveStores();
    }
    
    public boolean validateAccessCode(Store store, String accessCode) {
        return passwordEncoder.matches(accessCode, store.getAccessCode());
    }
    
    public boolean validateAccessCode(Long storeId, String accessCode) {
        Store store = getStoreById(storeId);
        return validateAccessCode(store, accessCode);
    }
    
    public boolean validateAccessCodeByCode(String storeCode, String accessCode) {
        Store store = getStoreByCode(storeCode);
        return validateAccessCode(store, accessCode);
    }

    /**
     * Check if a store exists by its code (can be alphanumeric like STR001, CRK2645, etc.)
     * @param storeCode The store code to check
     * @return true if store exists, false otherwise
     */
    public boolean existsByCode(String storeCode) {
        return storeRepository.existsByCode(storeCode);
    }
    
    @Transactional
    public void deleteStore(Long storeId, String accessCode) {
        log.info("Deleting store with ID: {}", storeId);
        
        Store store = getStoreById(storeId);
        
        // Validate access code
        if (!validateAccessCode(store, accessCode)) {
            log.warn("Invalid access code provided for store deletion, ID: {}", storeId);
            throw new IllegalArgumentException("Invalid access code");
        }
        
        // Soft delete by setting isActive to false
        store.setIsActive(false);
        storeRepository.save(store);
        
        log.info("Store soft deleted successfully: {}", storeId);
    }
}

