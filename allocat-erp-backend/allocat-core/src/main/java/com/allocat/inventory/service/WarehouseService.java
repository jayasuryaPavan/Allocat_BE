package com.allocat.inventory.service;

import com.allocat.auth.entity.Store;
import com.allocat.auth.repository.StoreRepository;
import com.allocat.inventory.entity.Warehouse;
import com.allocat.inventory.entity.WarehouseLocation;
import com.allocat.inventory.repository.WarehouseLocationRepository;
import com.allocat.inventory.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseLocationRepository warehouseLocationRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public Warehouse createWarehouse(Warehouse warehouse) {
        log.info("Creating warehouse: {}", warehouse.getCode());

        if (warehouseRepository.existsByCode(warehouse.getCode())) {
            throw new RuntimeException("Warehouse with code already exists: " + warehouse.getCode());
        }

        if (warehouse.getStore() != null && warehouse.getStore().getId() != null) {
            Store store = storeRepository.findById(warehouse.getStore().getId())
                    .orElseThrow(() -> new RuntimeException("Store not found: " + warehouse.getStore().getId()));
            warehouse.setStore(store);
        }

        return warehouseRepository.save(warehouse);
    }

    @Transactional
    public Warehouse updateWarehouse(Long warehouseId, Warehouse updatedWarehouse) {
        log.info("Updating warehouse: {}", warehouseId);

        Warehouse existing = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Warehouse not found: " + warehouseId));

        if (updatedWarehouse.getName() != null) {
            existing.setName(updatedWarehouse.getName());
        }
        if (updatedWarehouse.getType() != null) {
            existing.setType(updatedWarehouse.getType());
        }
        if (updatedWarehouse.getAddress() != null) {
            existing.setAddress(updatedWarehouse.getAddress());
        }
        if (updatedWarehouse.getCity() != null) {
            existing.setCity(updatedWarehouse.getCity());
        }
        if (updatedWarehouse.getState() != null) {
            existing.setState(updatedWarehouse.getState());
        }
        if (updatedWarehouse.getCountry() != null) {
            existing.setCountry(updatedWarehouse.getCountry());
        }
        if (updatedWarehouse.getPostalCode() != null) {
            existing.setPostalCode(updatedWarehouse.getPostalCode());
        }
        if (updatedWarehouse.getPhone() != null) {
            existing.setPhone(updatedWarehouse.getPhone());
        }
        if (updatedWarehouse.getEmail() != null) {
            existing.setEmail(updatedWarehouse.getEmail());
        }
        if (updatedWarehouse.getManager() != null) {
            existing.setManager(updatedWarehouse.getManager());
        }
        if (updatedWarehouse.getIsActive() != null) {
            existing.setIsActive(updatedWarehouse.getIsActive());
        }
        if (updatedWarehouse.getSettings() != null) {
            existing.setSettings(updatedWarehouse.getSettings());
        }

        return warehouseRepository.save(existing);
    }

    public Warehouse getWarehouseById(Long warehouseId) {
        return warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Warehouse not found: " + warehouseId));
    }

    public Warehouse getWarehouseByCode(String code) {
        return warehouseRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Warehouse not found: " + code));
    }

    public List<Warehouse> getWarehousesByStore(Long storeId) {
        return warehouseRepository.findByStoreIdAndIsActive(storeId, true);
    }

    public List<Warehouse> getAllActiveWarehouses() {
        return warehouseRepository.findByIsActive(true);
    }

    @Transactional
    public WarehouseLocation createLocation(WarehouseLocation location) {
        log.info("Creating warehouse location: {} in warehouse {}", 
                 location.getCode(), location.getWarehouse().getId());

        if (warehouseLocationRepository.findByWarehouseIdAndCode(
                location.getWarehouse().getId(), location.getCode()).isPresent()) {
            throw new RuntimeException("Location with code already exists in warehouse: " + location.getCode());
        }

        return warehouseLocationRepository.save(location);
    }

    public List<WarehouseLocation> getLocationsByWarehouse(Long warehouseId) {
        return warehouseLocationRepository.findByWarehouseIdAndIsActive(warehouseId, true);
    }

    @Transactional
    public void deleteWarehouse(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Warehouse not found: " + warehouseId));
        
        warehouse.setIsActive(false);
        warehouseRepository.save(warehouse);
        log.info("Warehouse deactivated: {}", warehouseId);
    }
}
