package com.allocat.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    @Schema(description = "JWT access token")
    private String accessToken;
    
    @Schema(description = "JWT refresh token")
    private String refreshToken;
    
    @Schema(description = "Token type", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";
    
    @Schema(description = "Token expiration time in milliseconds")
    private Long expiresIn;
    
    @Schema(description = "User ID")
    private Long userId;
    
    @Schema(description = "Username")
    private String username;
    
    @Schema(description = "User's email")
    private String email;
    
    @Schema(description = "User's first name")
    private String firstName;
    
    @Schema(description = "User's last name")
    private String lastName;
    
    @Schema(description = "User's phone number")
    private String phone;
    
    @Schema(description = "User's role ID")
    private Long roleId;
    
    @Schema(description = "User's role name")
    private String role;
    
    @Schema(description = "User's store ID")
    private Long storeId;
    
    @Schema(description = "User's store code")
    private String storeCode;
    
    @Schema(description = "User's store name")
    private String storeName;
    
    @Schema(description = "Whether the user account is active")
    private Boolean isActive;
    
    @Schema(description = "Last login timestamp")
    private String lastLoginAt;
    
    @Schema(description = "User's permissions array")
    private String[] permissions;
}

