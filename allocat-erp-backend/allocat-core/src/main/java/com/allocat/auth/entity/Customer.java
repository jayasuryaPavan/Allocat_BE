package com.allocat.auth.entity;

import com.allocat.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Customer extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_code", unique = true, nullable = false, length = 50)
    private String customerCode;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "email", length = 100)
    private String email;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "address")
    private String address;
    
    @Column(name = "city", length = 100)
    private String city;
    
    @Column(name = "state", length = 100)
    private String state;
    
    @Column(name = "country", length = 100)
    private String country;
    
    @Column(name = "postal_code", length = 20)
    private String postalCode;
    
    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber;
    
    @Column(name = "tax_id", length = 50)
    private String taxId;
    
    @Column(name = "company_name", length = 200)
    private String companyName;
    
    @Column(name = "contact_person", length = 100)
    private String contactPerson;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;
}













