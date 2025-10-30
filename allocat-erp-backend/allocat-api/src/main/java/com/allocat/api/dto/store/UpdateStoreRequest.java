package com.allocat.api.dto.store;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStoreRequest {
    
    @Schema(example = "ACCESS2024!", description = "Access code for verification")
    @NotBlank(message = "Access code is required for updates")
    private String accessCode;
    
    @Schema(example = "Main Street Store - Updated")
    @Size(max = 100, message = "Store name must not exceed 100 characters")
    private String name;
    
    @Schema(example = "123 Main Street, Suite 100")
    private String address;
    
    @Schema(example = "New York")
    private String city;
    
    @Schema(example = "NY")
    private String state;
    
    @Schema(example = "USA")
    private String country;
    
    @Schema(example = "10001")
    private String postalCode;
    
    @Schema(example = "+1234567890")
    private String phone;
    
    @Schema(example = "store@example.com")
    private String email;
    
    @Schema(example = "TAX-123456")
    private String taxId;
    
    @Schema(example = "USD")
    private String currency;
    
    @Schema(example = "America/New_York")
    private String timezone;
    
    @Schema(example = "true", description = "Store active status")
    private Boolean isActive;
}

