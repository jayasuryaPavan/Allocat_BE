package com.allocat.api.dto.store;

import com.allocat.auth.entity.Store;
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
public class StoreResponse {
    
    @Schema(description = "Store ID")
    private Long id;
    
    @Schema(description = "Store code")
    private String code;
    
    @Schema(description = "Store name")
    private String name;
    
    @Schema(description = "Store address")
    private String address;
    
    @Schema(description = "City")
    private String city;
    
    @Schema(description = "State/Province")
    private String state;
    
    @Schema(description = "Country")
    private String country;
    
    @Schema(description = "Postal code")
    private String postalCode;
    
    @Schema(description = "Phone number")
    private String phone;
    
    @Schema(description = "Email address")
    private String email;
    
    @Schema(description = "Tax ID")
    private String taxId;
    
    @Schema(description = "Currency code")
    private String currency;
    
    @Schema(description = "Timezone")
    private String timezone;
    
    @Schema(description = "Store active status")
    private Boolean isActive;
    
    @Schema(description = "Created timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Updated timestamp")
    private LocalDateTime updatedAt;
    
    public static StoreResponse fromEntity(Store store) {
        return StoreResponse.builder()
                .id(store.getId())
                .code(store.getCode())
                .name(store.getName())
                .address(store.getAddress())
                .city(store.getCity())
                .state(store.getState())
                .country(store.getCountry())
                .postalCode(store.getPostalCode())
                .phone(store.getPhone())
                .email(store.getEmail())
                .taxId(store.getTaxId())
                .currency(store.getCurrency())
                .timezone(store.getTimezone())
                .isActive(store.getIsActive())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }
}

