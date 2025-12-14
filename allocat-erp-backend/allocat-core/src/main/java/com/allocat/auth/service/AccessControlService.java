package com.allocat.auth.service;

import com.allocat.auth.entity.User;
import com.allocat.auth.entity.UserStoreAccess;
import com.allocat.auth.repository.StoreRepository;
import com.allocat.auth.repository.UserRepository;
import com.allocat.auth.repository.UserStoreAccessRepository;
import com.allocat.inventory.entity.Warehouse;
import com.allocat.inventory.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccessControlService {

    private final UserStoreAccessRepository userStoreAccessRepository;
    private final UserRepository userRepository;
    private final WarehouseRepository warehouseRepository;
    private final StoreRepository storeRepository;

    /**
     * Check if user has access to a store
     */
    public boolean hasStoreAccess(Long userId, Long storeId, UserStoreAccess.AccessLevel requiredLevel) {
        // SUPER_ADMIN and ADMIN have access to all stores
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        if (isSuperAdminOrAdmin(user)) {
            return true;
        }

        List<UserStoreAccess> accesses = userStoreAccessRepository.findActiveAccessByUserAndStore(
                userId, storeId, LocalDateTime.now());

        if (accesses.isEmpty()) {
            return false;
        }

        // Check if any access meets the required level
        return accesses.stream()
                .anyMatch(access -> hasRequiredLevel(access.getAccessLevel(), requiredLevel));
    }

    /**
     * Check if user has access to a warehouse
     */
    public boolean hasWarehouseAccess(Long userId, Long warehouseId, UserStoreAccess.AccessLevel requiredLevel) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        if (isSuperAdminOrAdmin(user)) {
            return true;
        }

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Warehouse not found: " + warehouseId));

        // Check warehouse-specific access
        Optional<UserStoreAccess> warehouseAccess = userStoreAccessRepository
                .findByUserIdAndStoreIdAndWarehouseId(userId, warehouse.getStore().getId(), warehouseId);

        if (warehouseAccess.isPresent() && warehouseAccess.get().getIsActive() &&
            (warehouseAccess.get().getExpiresAt() == null || 
             warehouseAccess.get().getExpiresAt().isAfter(LocalDateTime.now()))) {
            return hasRequiredLevel(warehouseAccess.get().getAccessLevel(), requiredLevel);
        }

        // Check store-level access (warehouse access implies store access)
        return hasStoreAccess(userId, warehouse.getStore().getId(), requiredLevel);
    }

    /**
     * Get all store IDs the user has access to
     */
    public List<Long> getAccessibleStoreIds(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        if (isSuperAdminOrAdmin(user)) {
            // Return all active store IDs
            return storeRepository.findByIsActive(true).stream()
                    .map(store -> store.getId())
                    .collect(Collectors.toList());
        }

        List<UserStoreAccess> accesses = userStoreAccessRepository.findActiveAccessByUser(
                userId, LocalDateTime.now());

        return accesses.stream()
                .map(access -> access.getStore().getId())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Get all warehouse IDs the user has access to
     */
    public List<Long> getAccessibleWarehouseIds(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        if (isSuperAdminOrAdmin(user)) {
            // Return all active warehouse IDs
            return warehouseRepository.findByIsActive(true).stream()
                    .map(warehouse -> warehouse.getId())
                    .collect(Collectors.toList());
        }

        List<UserStoreAccess> accesses = userStoreAccessRepository.findActiveAccessByUser(
                userId, LocalDateTime.now());

        return accesses.stream()
                .filter(access -> access.getWarehouse() != null)
                .map(access -> access.getWarehouse().getId())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Get user's primary store ID
     */
    public Long getPrimaryStoreId(Long userId) {
        return userStoreAccessRepository.findPrimaryStoreAccess(userId)
                .map(access -> access.getStore().getId())
                .orElse(null);
    }

    /**
     * Get user's access level for a store
     */
    public UserStoreAccess.AccessLevel getStoreAccessLevel(Long userId, Long storeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        if (isSuperAdminOrAdmin(user)) {
            return UserStoreAccess.AccessLevel.ADMIN;
        }

        List<UserStoreAccess> accesses = userStoreAccessRepository.findActiveAccessByUserAndStore(
                userId, storeId, LocalDateTime.now());

        if (accesses.isEmpty()) {
            return null;
        }

        // Return the highest access level
        return accesses.stream()
                .map(UserStoreAccess::getAccessLevel)
                .max((a1, a2) -> {
                    int level1 = getAccessLevelValue(a1);
                    int level2 = getAccessLevelValue(a2);
                    return Integer.compare(level1, level2);
                })
                .orElse(UserStoreAccess.AccessLevel.VIEW);
    }

    /**
     * Verify user can perform operation on store
     */
    public void verifyStoreAccess(Long userId, Long storeId, UserStoreAccess.AccessLevel requiredLevel) {
        if (!hasStoreAccess(userId, storeId, requiredLevel)) {
            throw new RuntimeException("User does not have " + requiredLevel.name() + 
                                     " access to store: " + storeId);
        }
    }

    /**
     * Verify user can perform operation on warehouse
     */
    public void verifyWarehouseAccess(Long userId, Long warehouseId, UserStoreAccess.AccessLevel requiredLevel) {
        if (!hasWarehouseAccess(userId, warehouseId, requiredLevel)) {
            throw new RuntimeException("User does not have " + requiredLevel.name() + 
                                     " access to warehouse: " + warehouseId);
        }
    }

    // Helper methods
    private boolean isSuperAdminOrAdmin(User user) {
        if (user.getRole() == null) {
            return false;
        }
        String roleName = user.getRole().getName();
        return "SUPER_ADMIN".equals(roleName) || "ADMIN".equals(roleName);
    }

    private boolean hasRequiredLevel(UserStoreAccess.AccessLevel userLevel, 
                                    UserStoreAccess.AccessLevel requiredLevel) {
        int userLevelValue = getAccessLevelValue(userLevel);
        int requiredLevelValue = getAccessLevelValue(requiredLevel);
        return userLevelValue >= requiredLevelValue;
    }

    private int getAccessLevelValue(UserStoreAccess.AccessLevel level) {
        return switch (level) {
            case VIEW -> 1;
            case OPERATE -> 2;
            case MANAGE -> 3;
            case ADMIN -> 4;
        };
    }
}
