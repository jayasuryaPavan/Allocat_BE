package com.allocat.auth.entity;

import com.allocat.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entity representing user-specific settings and preferences.
 * Stores encrypted API keys and other user configuration.
 */
@Entity
@Table(name = "user_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserSettings extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    /**
     * Encrypted Gemini API key for user-specific AI requests.
     * Stored as AES-256 encrypted string.
     */
    @Column(name = "gemini_api_key", length = 512)
    private String geminiApiKey;
}
