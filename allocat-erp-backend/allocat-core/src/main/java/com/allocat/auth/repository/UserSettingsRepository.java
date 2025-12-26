package com.allocat.auth.repository;

import com.allocat.auth.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing UserSettings entities.
 */
@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {

    /**
     * Find user settings by user ID.
     *
     * @param userId the user ID
     * @return Optional containing UserSettings if found
     */
    Optional<UserSettings> findByUserId(Long userId);

    /**
     * Check if settings exist for a user.
     *
     * @param userId the user ID
     * @return true if settings exist
     */
    boolean existsByUserId(Long userId);
}
