package com.allocat.auth.entity;

import com.allocat.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Store extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "code", unique = true, nullable = false, length = 20)
    private String code;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "access_code", nullable = false, length = 50)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String accessCode;
    
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
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "email", length = 100)
    private String email;
    
    @Column(name = "tax_id", length = 50)
    private String taxId;
    
    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "USD";
    
    @Column(name = "timezone", length = 50)
    @Builder.Default
    private String timezone = "UTC";
    
    @Column(name = "settings", columnDefinition = "jsonb")
    private String settings;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}

