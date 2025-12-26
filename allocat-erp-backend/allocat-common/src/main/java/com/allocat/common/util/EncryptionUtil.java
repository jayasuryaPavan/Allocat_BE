package com.allocat.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * Utility class for encrypting and decrypting sensitive data like API keys.
 * Uses AES-256 encryption.
 */
@Component
@Slf4j
public class EncryptionUtil {

    private static final String ALGORITHM = "AES";

    @Value("${encryption.secret:allocat-secret-key-change-in-production}")
    private String secretKey;

    /**
     * Encrypt plaintext using AES-256.
     *
     * @param plainText the text to encrypt
     * @return Base64-encoded encrypted string
     */
    public String encrypt(String plainText) {
        try {
            SecretKey key = generateKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    /**
     * Decrypt Base64-encoded ciphertext using AES-256.
     *
     * @param cipherText the encrypted text (Base64-encoded)
     * @return decrypted plaintext
     */
    public String decrypt(String cipherText) {
        try {
            SecretKey key = generateKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Decryption failed", e);
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }

    /**
     * Generate AES secret key from the configured secret string.
     */
    private SecretKey generateKey() throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        keyBytes = sha.digest(keyBytes);
        keyBytes = Arrays.copyOf(keyBytes, 16); // Use only first 128 bits
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * Mask an API key for display purposes.
     * Shows only first 6 and last 4 characters.
     *
     * @param apiKey the API key to mask
     * @return masked string like "sk-abc...xyz"
     */
    public String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 10) {
            return "****";
        }
        return apiKey.substring(0, Math.min(6, apiKey.length())) +
                "..." +
                apiKey.substring(Math.max(0, apiKey.length() - 4));
    }
}
