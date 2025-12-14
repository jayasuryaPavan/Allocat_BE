package com.allocat.api.controller;

import com.allocat.common.dto.ApiResponse;
import com.allocat.pos.entity.Discount;
import com.allocat.pos.service.DiscountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Discount management
 */
@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Discounts", description = "APIs for managing discounts and promotions")
public class DiscountController {

    private final DiscountService discountService;

    @GetMapping
    @Operation(summary = "Get all discounts", description = "Retrieve all discounts or only active ones")
    public ResponseEntity<ApiResponse<List<Discount>>> getDiscounts(
            @Parameter(description = "Filter by active status") @RequestParam(required = false, defaultValue = "false") Boolean activeOnly) {
        try {
            List<Discount> discounts = activeOnly ? discountService.getActiveDiscounts()
                    : discountService.getAllDiscounts();

            return ResponseEntity.ok(ApiResponse.success(discounts,
                    "Retrieved " + discounts.size() + " discount(s)"));
        } catch (Exception e) {
            log.error("Error retrieving discounts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving discounts: " + e.getMessage()));
        }
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get discount by code", description = "Retrieve a discount by its code")
    public ResponseEntity<ApiResponse<Discount>> getDiscountByCode(
            @Parameter(description = "Discount code") @PathVariable String code) {
        try {
            Discount discount = discountService.getDiscountByCode(code);
            return ResponseEntity.ok(ApiResponse.success(discount));
        } catch (Exception e) {
            log.error("Error retrieving discount: {}", code, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Discount not found: " + e.getMessage()));
        }
    }

    @PostMapping
    @Operation(summary = "Create discount", description = "Create a new discount or promotion")
    public ResponseEntity<ApiResponse<Discount>> createDiscount(@RequestBody Discount discount) {
        try {
            Discount created = discountService.createDiscount(discount);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(created, "Discount created successfully"));
        } catch (Exception e) {
            log.error("Error creating discount", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error creating discount: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update discount", description = "Update an existing discount")
    public ResponseEntity<ApiResponse<Discount>> updateDiscount(
            @Parameter(description = "Discount ID") @PathVariable Long id,
            @RequestBody Discount discount) {
        try {
            Discount updated = discountService.updateDiscount(id, discount);
            return ResponseEntity.ok(ApiResponse.success(updated, "Discount updated successfully"));
        } catch (Exception e) {
            log.error("Error updating discount: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error updating discount: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate discount", description = "Deactivate a discount")
    public ResponseEntity<ApiResponse<Void>> deactivateDiscount(
            @Parameter(description = "Discount ID") @PathVariable Long id) {
        try {
            discountService.deactivateDiscount(id);
            return ResponseEntity.ok(ApiResponse.success(null, "Discount deactivated successfully"));
        } catch (Exception e) {
            log.error("Error deactivating discount: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error deactivating discount: " + e.getMessage()));
        }
    }
}
