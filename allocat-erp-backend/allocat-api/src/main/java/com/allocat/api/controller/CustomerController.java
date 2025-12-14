package com.allocat.api.controller;

import com.allocat.api.dto.customer.CreateCustomerRequest;
import com.allocat.api.dto.customer.CustomerResponse;
import com.allocat.api.dto.customer.UpdateCustomerRequest;
import com.allocat.auth.entity.Customer;
import com.allocat.auth.entity.Store;
import com.allocat.auth.service.CustomerService;
import com.allocat.auth.service.StoreService;
import com.allocat.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Management", description = "APIs for managing customers")
@SecurityRequirement(name = "Bearer Authentication")
public class CustomerController {

    private final CustomerService customerService;
    private final StoreService storeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Create a new customer", description = "Create a new customer with invoice number and store association. Requires SUPER_ADMIN, ADMIN, or MANAGER role.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Customer created successfully",
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad request - validation error or duplicate customer code"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        try {
            Store store = storeService.getStoreById(request.getStoreId());
            
            Customer customer = Customer.builder()
                    .customerCode(request.getCustomerCode())
                    .name(request.getName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .address(request.getAddress())
                    .city(request.getCity())
                    .state(request.getState())
                    .country(request.getCountry())
                    .postalCode(request.getPostalCode())
                    .invoiceNumber(request.getInvoiceNumber())
                    .taxId(request.getTaxId())
                    .companyName(request.getCompanyName())
                    .contactPerson(request.getContactPerson())
                    .notes(request.getNotes())
                    .isActive(true)
                    .store(store)
                    .build();
            
            Customer createdCustomer = customerService.createCustomer(customer);
            
            return ResponseEntity.ok(ApiResponse.success(
                    CustomerResponse.fromEntity(createdCustomer),
                    "Customer created successfully"
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error creating customer: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error creating customer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error creating customer: " + e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'VIEWER')")
    @Operation(summary = "Get all customers", description = "Retrieve all customers with optional filtering by store, active status, and search term. Supports filtering by store ID, active status, and search by name, code, email, or invoice number.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Customers retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers(
            @Parameter(description = "Filter by store ID")
            @RequestParam(required = false) Long storeId,
            @Parameter(description = "Filter by active status")
            @RequestParam(required = false) Boolean active,
            @Parameter(description = "Search term for name, code, email, or invoice number")
            @RequestParam(required = false) String search) {
        try {
            List<Customer> customers;
            
            // If storeId is provided, filter by store
            if (storeId != null) {
                if (search != null && !search.isEmpty()) {
                    customers = customerService.searchCustomersByStore(storeId, search);
                } else if (active != null && active) {
                    customers = customerService.getActiveCustomersByStoreId(storeId);
                } else {
                    customers = customerService.getCustomersByStoreId(storeId);
                }
            } else {
                // Get all customers (SUPER_ADMIN only)
                customers = customerService.getAllCustomers();
                if (active != null && active) {
                    customers = customers.stream()
                            .filter(c -> c.getIsActive())
                            .collect(Collectors.toList());
                }
            }
            
            List<CustomerResponse> customerResponses = customers.stream()
                    .map(CustomerResponse::fromEntity)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(
                    customerResponses,
                    "Customers retrieved successfully"
            ));
        } catch (Exception e) {
            log.error("Error retrieving customers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving customers: " + e.getMessage()));
        }
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'VIEWER')")
    @Operation(summary = "Get customer by ID", description = "Retrieve customer details by customer ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Customer retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Customer not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(
            @Parameter(description = "Customer ID") 
            @PathVariable Long customerId) {
        try {
            Customer customer = customerService.getCustomerById(customerId);
            return ResponseEntity.ok(ApiResponse.success(
                    CustomerResponse.fromEntity(customer),
                    "Customer retrieved successfully"
            ));
        } catch (IllegalArgumentException e) {
            log.error("Customer not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving customer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving customer: " + e.getMessage()));
        }
    }

    @GetMapping("/code/{customerCode}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'VIEWER')")
    @Operation(summary = "Get customer by code", description = "Retrieve customer details by customer code")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Customer retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Customer not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerByCode(
            @Parameter(description = "Customer code") 
            @PathVariable String customerCode) {
        try {
            Customer customer = customerService.getCustomerByCode(customerCode);
            return ResponseEntity.ok(ApiResponse.success(
                    CustomerResponse.fromEntity(customer),
                    "Customer retrieved successfully"
            ));
        } catch (IllegalArgumentException e) {
            log.error("Customer not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving customer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving customer: " + e.getMessage()));
        }
    }

    @GetMapping("/invoice/{invoiceNumber}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'VIEWER')")
    @Operation(summary = "Get customers by invoice number", description = "Retrieve all customers associated with a specific invoice number. Optionally filter by store ID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Customers retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getCustomersByInvoiceNumber(
            @Parameter(description = "Invoice number") 
            @PathVariable String invoiceNumber,
            @Parameter(description = "Filter by store ID")
            @RequestParam(required = false) Long storeId) {
        try {
            List<Customer> customers;
            if (storeId != null) {
                customers = customerService.getCustomersByStoreId(storeId).stream()
                        .filter(c -> invoiceNumber.equals(c.getInvoiceNumber()))
                        .collect(Collectors.toList());
            } else {
                customers = customerService.getCustomersByInvoiceNumber(invoiceNumber);
            }
            
            List<CustomerResponse> customerResponses = customers.stream()
                    .map(CustomerResponse::fromEntity)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(
                    customerResponses,
                    "Customers retrieved successfully"
            ));
        } catch (Exception e) {
            log.error("Error retrieving customers by invoice number", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving customers: " + e.getMessage()));
        }
    }

    @PutMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Update customer", description = "Update customer details including invoice number. Only provided fields will be updated. Requires SUPER_ADMIN, ADMIN, or MANAGER role.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Customer updated successfully",
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad request - validation error or duplicate customer code"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Customer not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @Parameter(description = "Customer ID") 
            @PathVariable Long customerId,
            @Valid @RequestBody UpdateCustomerRequest request) {
        try {
            Customer updatedData = Customer.builder()
                    .customerCode(request.getCustomerCode())
                    .name(request.getName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .address(request.getAddress())
                    .city(request.getCity())
                    .state(request.getState())
                    .country(request.getCountry())
                    .postalCode(request.getPostalCode())
                    .invoiceNumber(request.getInvoiceNumber())
                    .taxId(request.getTaxId())
                    .companyName(request.getCompanyName())
                    .contactPerson(request.getContactPerson())
                    .notes(request.getNotes())
                    .isActive(request.getIsActive())
                    .build();
            
            Customer updatedCustomer = customerService.updateCustomer(customerId, updatedData);
            
            return ResponseEntity.ok(ApiResponse.success(
                    CustomerResponse.fromEntity(updatedCustomer),
                    "Customer updated successfully"
            ));
        } catch (IllegalArgumentException e) {
            log.error("Error updating customer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error updating customer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error updating customer: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Delete customer", description = "Soft delete a customer by setting isActive to false. Requires SUPER_ADMIN, ADMIN, or MANAGER role.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Customer deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad request - customer not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient permissions"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(
            @Parameter(description = "Customer ID") 
            @PathVariable Long customerId) {
        try {
            customerService.deleteCustomer(customerId);
            return ResponseEntity.ok(ApiResponse.success(null, "Customer deleted successfully"));
        } catch (IllegalArgumentException e) {
            log.error("Error deleting customer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error deleting customer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting customer: " + e.getMessage()));
        }
    }
}

