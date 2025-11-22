package com.allocat.api.dto.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerRequest {
    
    @Schema(example = "CUST001", description = "Unique customer code")
    @NotBlank(message = "Customer code cannot be blank")
    @Size(max = 50, message = "Customer code must not exceed 50 characters")
    private String customerCode;
    
    @Schema(example = "John Doe", description = "Customer name")
    @NotBlank(message = "Customer name cannot be blank")
    @Size(max = 100, message = "Customer name must not exceed 100 characters")
    private String name;
    
    @Schema(example = "john.doe@example.com")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;
    
    @Schema(example = "+1234567890")
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;
    
    @Schema(example = "123 Main Street")
    private String address;
    
    @Schema(example = "New York")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;
    
    @Schema(example = "NY")
    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;
    
    @Schema(example = "USA")
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;
    
    @Schema(example = "10001")
    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;
    
    @Schema(example = "INV-2024-001", description = "Invoice number")
    @Size(max = 100, message = "Invoice number must not exceed 100 characters")
    private String invoiceNumber;
    
    @Schema(example = "TAX-123456")
    @Size(max = 50, message = "Tax ID must not exceed 50 characters")
    private String taxId;
    
    @Schema(example = "ABC Corporation")
    @Size(max = 200, message = "Company name must not exceed 200 characters")
    private String companyName;
    
    @Schema(example = "Jane Smith")
    @Size(max = 100, message = "Contact person must not exceed 100 characters")
    private String contactPerson;
    
    @Schema(example = "Important customer notes")
    private String notes;
    
    @Schema(example = "1", description = "Store ID")
    @NotNull(message = "Store ID is required")
    private Long storeId;
}















