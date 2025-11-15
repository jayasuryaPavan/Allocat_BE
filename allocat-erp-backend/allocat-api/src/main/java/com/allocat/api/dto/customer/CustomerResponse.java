package com.allocat.api.dto.customer;

import com.allocat.auth.entity.Customer;
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
public class CustomerResponse {
    
    @Schema(description = "Customer ID")
    private Long id;
    
    @Schema(description = "Customer code")
    private String customerCode;
    
    @Schema(description = "Customer name")
    private String name;
    
    @Schema(description = "Email address")
    private String email;
    
    @Schema(description = "Phone number")
    private String phone;
    
    @Schema(description = "Address")
    private String address;
    
    @Schema(description = "City")
    private String city;
    
    @Schema(description = "State/Province")
    private String state;
    
    @Schema(description = "Country")
    private String country;
    
    @Schema(description = "Postal code")
    private String postalCode;
    
    @Schema(description = "Invoice number")
    private String invoiceNumber;
    
    @Schema(description = "Tax ID")
    private String taxId;
    
    @Schema(description = "Company name")
    private String companyName;
    
    @Schema(description = "Contact person")
    private String contactPerson;
    
    @Schema(description = "Notes")
    private String notes;
    
    @Schema(description = "Customer active status")
    private Boolean isActive;
    
    @Schema(description = "Store ID")
    private Long storeId;
    
    @Schema(description = "Store code")
    private String storeCode;
    
    @Schema(description = "Store name")
    private String storeName;
    
    @Schema(description = "Created timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Updated timestamp")
    private LocalDateTime updatedAt;
    
    public static CustomerResponse fromEntity(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .customerCode(customer.getCustomerCode())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .city(customer.getCity())
                .state(customer.getState())
                .country(customer.getCountry())
                .postalCode(customer.getPostalCode())
                .invoiceNumber(customer.getInvoiceNumber())
                .taxId(customer.getTaxId())
                .companyName(customer.getCompanyName())
                .contactPerson(customer.getContactPerson())
                .notes(customer.getNotes())
                .isActive(customer.getIsActive())
                .storeId(customer.getStore().getId())
                .storeCode(customer.getStore().getCode())
                .storeName(customer.getStore().getName())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}












