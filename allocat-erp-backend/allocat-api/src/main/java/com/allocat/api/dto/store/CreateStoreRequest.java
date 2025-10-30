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
public class CreateStoreRequest {
    
    @Schema(example = "ST001", description = "Unique store code")
    @NotBlank(message = "Store code cannot be blank")
    @Size(max = 20, message = "Store code must not exceed 20 characters")
    private String code;
    
    @Schema(example = "Main Street Store", description = "Store name")
    @NotBlank(message = "Store name cannot be blank")
    @Size(max = 100, message = "Store name must not exceed 100 characters")
    private String name;
    
    @Schema(example = "ACCESS2024!", description = "Access code for store management")
    @NotBlank(message = "Access code cannot be blank")
    @Size(min = 6, max = 50, message = "Access code must be between 6 and 50 characters")
    private String accessCode;
    
    @Schema(example = "123 Main Street")
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
    
    @Schema(example = "USD", description = "Currency code (ISO 4217)")
    private String currency;
    
    @Schema(example = "America/New_York", description = "Timezone")
    private String timezone;
}

