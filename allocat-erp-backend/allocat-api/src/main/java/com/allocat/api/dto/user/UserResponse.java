package com.allocat.api.dto.user;

import com.allocat.auth.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    @Schema(description = "User ID")
    private Long id;
    
    @Schema(description = "Username")
    private String username;
    
    @Schema(description = "Email address")
    private String email;
    
    @Schema(description = "First name")
    private String firstName;
    
    @Schema(description = "Last name")
    private String lastName;
    
    @Schema(description = "Phone number")
    private String phone;
    
    @Schema(description = "Role name")
    private String roleName;
    
    @Schema(description = "Store ID")
    private Long storeId;
    
    @Schema(description = "Store code")
    private String storeCode;
    
    @Schema(description = "Store name")
    private String storeName;
    
    @Schema(description = "Active status")
    private Boolean active;
    
    @Schema(description = "Last login timestamp")
    private LocalDateTime lastLoginAt;
    
    @Schema(description = "Created timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Updated timestamp")
    private LocalDateTime updatedAt;
    
    public static UserResponse fromEntity(User user, String storeCode, String storeName) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .storeId(user.getStoreId())
                .storeCode(storeCode)
                .storeName(storeName)
                .active(user.getActive())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

