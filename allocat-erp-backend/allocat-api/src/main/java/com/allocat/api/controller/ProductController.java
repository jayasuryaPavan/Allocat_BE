package com.allocat.api.controller;

import com.allocat.common.dto.ApiResponse;
import com.allocat.inventory.entity.Product;
import com.allocat.inventory.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Management", description = "APIs for managing products")
public class ProductController {

    private final ProductRepository productRepository;

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve all products with pagination, sorting, and filtering")
    public ResponseEntity<ApiResponse<Page<Product>>> getAllProducts(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field (e.g., 'name', 'productCode', 'unitPrice', 'category')") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction: 'asc' or 'desc'") @RequestParam(defaultValue = "asc") String sortDirection,
            @Parameter(description = "Search term for product name, code, or barcode") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by category") @RequestParam(required = false) String category,
            @Parameter(description = "Filter by supplier name") @RequestParam(required = false) String supplier,
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean active) {
        try {
            // Create sort object
            Sort sort = sortDirection.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Product> products;

            if (search != null && !search.trim().isEmpty()) {
                products = productRepository.findByNameContainingIgnoreCase(search, pageable);
            } else if (category != null && !category.trim().isEmpty()) {
                products = productRepository.findByCategory(category, pageable);
            } else if (supplier != null && !supplier.trim().isEmpty()) {
                products = productRepository.findBySupplierName(supplier, pageable);
            } else if (active != null && active) {
                products = productRepository.findByIsActiveTrue(pageable);
            } else {
                products = productRepository.findAll(pageable);
            }

            return ResponseEntity.ok(ApiResponse.<Page<Product>>builder()
                    .success(true)
                    .message("Products retrieved successfully. Page " + (page + 1) + " of " + products.getTotalPages())
                    .data(products)
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Page<Product>>builder()
                            .success(false)
                            .message("Error retrieving products: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve a specific product by its ID")
    public ResponseEntity<ApiResponse<Product>> getProductById(
            @Parameter(description = "Product ID") @PathVariable long id) {
        try {
            return productRepository.findById(id)
                    .map(product -> ResponseEntity.ok(ApiResponse.<Product>builder()
                            .success(true)
                            .message("Product retrieved successfully")
                            .data(product)
                            .build()))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error retrieving product by ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Product>builder()
                            .success(false)
                            .message("Error retrieving product: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/code/{productCode}")
    @Operation(summary = "Get product by product code", description = "Retrieve a specific product by its product code")
    public ResponseEntity<ApiResponse<Product>> getProductByCode(
            @Parameter(description = "Product code") @PathVariable String productCode) {
        try {
            return productRepository.findByProductCode(productCode)
                    .map(product -> ResponseEntity.ok(ApiResponse.<Product>builder()
                            .success(true)
                            .message("Product retrieved successfully")
                            .data(product)
                            .build()))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error retrieving product by code", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Product>builder()
                            .success(false)
                            .message("Error retrieving product: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping
    @Operation(summary = "Create new product", description = "Create a new product in the system")
    public ResponseEntity<ApiResponse<Product>> createProduct(
            @Parameter(description = "Product details") @RequestBody Product product) {
        try {
            // Check if product code already exists
            if (productRepository.existsByProductCode(product.getProductCode())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<Product>builder()
                                .success(false)
                                .message("Product with code " + product.getProductCode() + " already exists")
                                .build());
            }

            // Check if barcode already exists (if provided)
            if (product.getBarcode() != null && productRepository.existsByBarcode(product.getBarcode())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<Product>builder()
                                .success(false)
                                .message("Product with barcode " + product.getBarcode() + " already exists")
                                .build());
            }

            Product savedProduct = productRepository.save(product);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<Product>builder()
                            .success(true)
                            .message("Product created successfully")
                            .data(savedProduct)
                            .build());
        } catch (Exception e) {
            log.error("Error creating product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Product>builder()
                            .success(false)
                            .message("Error creating product: " + e.getMessage())
                            .build());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Update an existing product")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @Parameter(description = "Product ID") @PathVariable long id,
            @Parameter(description = "Updated product details") @RequestBody Product product) {
        try {
            Optional<Product> existingProduct = productRepository.findById(id);
            if (existingProduct.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Check if product code is being changed and if it already exists
            if (!existingProduct.get().getProductCode().equals(product.getProductCode()) &&
                    productRepository.existsByProductCode(product.getProductCode())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<Product>builder()
                                .success(false)
                                .message("Product with code " + product.getProductCode() + " already exists")
                                .build());
            }

            // Check if barcode is being changed and if it already exists
            if (product.getBarcode() != null &&
                    !existingProduct.get().getBarcode().equals(product.getBarcode()) &&
                    productRepository.existsByBarcode(product.getBarcode())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.<Product>builder()
                                .success(false)
                                .message("Product with barcode " + product.getBarcode() + " already exists")
                                .build());
            }

            product.setId(id);
            Product updatedProduct = productRepository.save(product);
            return ResponseEntity.ok(ApiResponse.<Product>builder()
                    .success(true)
                    .message("Product updated successfully")
                    .data(updatedProduct)
                    .build());
        } catch (Exception e) {
            log.error("Error updating product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Product>builder()
                            .success(false)
                            .message("Error updating product: " + e.getMessage())
                            .build());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Delete a product (soft delete by setting isActive to false)")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable long id) {
        try {
            Optional<Product> product = productRepository.findById(id);
            if (product.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Soft delete by setting isActive to false
            Product productToDelete = product.get();
            productToDelete.setIsActive(false);
            productRepository.save(productToDelete);

            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Product deleted successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error deleting product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("Error deleting product: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search products by name, code, or barcode")
    public ResponseEntity<ApiResponse<List<Product>>> searchProducts(
            @Parameter(description = "Search term") @RequestParam String searchTerm) {
        try {
            List<Product> products = productRepository.searchProducts(searchTerm);
            return ResponseEntity.ok(ApiResponse.<List<Product>>builder()
                    .success(true)
                    .message("Search results retrieved successfully")
                    .data(products)
                    .build());
        } catch (Exception e) {
            log.error("Error searching products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<Product>>builder()
                            .success(false)
                            .message("Error searching products: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all product categories", description = "Retrieve all unique product categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        try {
            List<String> categories = productRepository.findDistinctCategories();
            return ResponseEntity.ok(ApiResponse.<List<String>>builder()
                    .success(true)
                    .message("Categories retrieved successfully")
                    .data(categories)
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving categories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<String>>builder()
                            .success(false)
                            .message("Error retrieving categories: " + e.getMessage())
                            .build());
        }
    }
}
