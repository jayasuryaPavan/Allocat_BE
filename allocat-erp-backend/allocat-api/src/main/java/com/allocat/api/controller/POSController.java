package com.allocat.api.controller;

import com.allocat.api.dto.pos.*;
import com.allocat.common.dto.ApiResponse;
import com.allocat.pos.dto.CartDTO;
import com.allocat.pos.entity.SalesOrder;
import com.allocat.pos.service.POSService;
import com.allocat.pos.service.PaymentService;
import com.allocat.pos.service.SalesOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Point of Sale operations
 */
@RestController
@RequestMapping("/api/pos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Point of Sale", description = "APIs for POS cart management and checkout")
public class POSController {

    private final POSService posService;
    private final SalesOrderService salesOrderService;
    private final PaymentService paymentService;

    @PostMapping("/cart")
    @Operation(summary = "Create new cart", description = "Create a new shopping cart for POS transaction")
    public ResponseEntity<ApiResponse<CartDTO>> createCart(@RequestBody CreateCartRequest request) {
        try {
            CartDTO cart = posService.createCart(request.getStoreId(), request.getCashierId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(cart, "Cart created successfully"));
        } catch (Exception e) {
            log.error("Error creating cart", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error creating cart: " + e.getMessage()));
        }
    }

    @GetMapping("/cart/{cartId}")
    @Operation(summary = "Get cart", description = "Retrieve cart details by cart ID")
    public ResponseEntity<ApiResponse<CartDTO>> getCart(
            @Parameter(description = "Cart ID") @PathVariable String cartId) {
        try {
            CartDTO cart = posService.getCart(cartId);
            return ResponseEntity.ok(ApiResponse.success(cart));
        } catch (Exception e) {
            log.error("Error retrieving cart: {}", cartId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Cart not found: " + e.getMessage()));
        }
    }

    @PostMapping("/cart/{cartId}/items")
    @Operation(summary = "Add item to cart", description = "Add a product to cart by product ID or barcode")
    public ResponseEntity<ApiResponse<CartDTO>> addItemToCart(
            @Parameter(description = "Cart ID") @PathVariable String cartId,
            @RequestBody AddItemRequest request) {
        try {
            CartDTO cart;
            if (request.getBarcode() != null && !request.getBarcode().isEmpty()) {
                cart = posService.addItemByBarcode(cartId, request.getBarcode(), request.getQuantity());
            } else {
                cart = posService.addItemToCart(cartId, request.getProductId(), request.getQuantity());
            }
            return ResponseEntity.ok(ApiResponse.success(cart, "Item added to cart"));
        } catch (Exception e) {
            log.error("Error adding item to cart: {}", cartId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error adding item: " + e.getMessage()));
        }
    }

    @PutMapping("/cart/{cartId}/items/{itemId}")
    @Operation(summary = "Update cart item", description = "Update the quantity of an item in the cart")
    public ResponseEntity<ApiResponse<CartDTO>> updateCartItem(
            @Parameter(description = "Cart ID") @PathVariable String cartId,
            @Parameter(description = "Item ID") @PathVariable String itemId,
            @RequestBody UpdateItemRequest request) {
        try {
            CartDTO cart = posService.updateCartItem(cartId, itemId, request.getQuantity());
            return ResponseEntity.ok(ApiResponse.success(cart, "Cart item updated"));
        } catch (Exception e) {
            log.error("Error updating cart item: {} in cart: {}", itemId, cartId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error updating item: " + e.getMessage()));
        }
    }

    @DeleteMapping("/cart/{cartId}/items/{itemId}")
    @Operation(summary = "Remove item from cart", description = "Remove an item from the cart")
    public ResponseEntity<ApiResponse<CartDTO>> removeItemFromCart(
            @Parameter(description = "Cart ID") @PathVariable String cartId,
            @Parameter(description = "Item ID") @PathVariable String itemId) {
        try {
            CartDTO cart = posService.removeItemFromCart(cartId, itemId);
            return ResponseEntity.ok(ApiResponse.success(cart, "Item removed from cart"));
        } catch (Exception e) {
            log.error("Error removing item: {} from cart: {}", itemId, cartId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error removing item: " + e.getMessage()));
        }
    }

    @PostMapping("/cart/{cartId}/discount")
    @Operation(summary = "Apply discount", description = "Apply a discount code to the cart")
    public ResponseEntity<ApiResponse<CartDTO>> applyDiscount(
            @Parameter(description = "Cart ID") @PathVariable String cartId,
            @Parameter(description = "Discount code") @RequestParam String code) {
        try {
            CartDTO cart = posService.applyDiscount(cartId, code);
            return ResponseEntity.ok(ApiResponse.success(cart, "Discount applied successfully"));
        } catch (Exception e) {
            log.error("Error applying discount to cart: {}", cartId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error applying discount: " + e.getMessage()));
        }
    }

    @DeleteMapping("/cart/{cartId}/discount")
    @Operation(summary = "Remove discount", description = "Remove the applied discount from cart")
    public ResponseEntity<ApiResponse<CartDTO>> removeDiscount(
            @Parameter(description = "Cart ID") @PathVariable String cartId) {
        try {
            CartDTO cart = posService.removeDiscount(cartId);
            return ResponseEntity.ok(ApiResponse.success(cart, "Discount removed"));
        } catch (Exception e) {
            log.error("Error removing discount from cart: {}", cartId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error removing discount: " + e.getMessage()));
        }
    }

    @PostMapping("/cart/{cartId}/checkout")
    @Operation(summary = "Checkout", description = "Complete the checkout process and create sales order")
    public ResponseEntity<ApiResponse<SalesOrder>> checkout(
            @Parameter(description = "Cart ID") @PathVariable String cartId,
            @RequestBody CheckoutRequest request) {
        try {
            // Get the cart
            CartDTO cart = posService.getCart(cartId);

            // Create sales order from cart
            SalesOrder order = salesOrderService.createSalesOrderFromCart(
                    cart,
                    request.getCustomerId(),
                    request.getNotes());

            // Process payments
            if (request.getPayments() != null && !request.getPayments().isEmpty()) {
                if (request.getPayments().size() == 1) {
                    // Single payment
                    PaymentRequest payReq = request.getPayments().get(0);
                    paymentService.processPayment(
                            order.getId(),
                            payReq.getPaymentType(),
                            payReq.getAmount(),
                            payReq.getTransactionId());
                } else {
                    // Split payment
                    paymentService.processSplitPayment(
                            order.getId(),
                            request.getPayments().stream()
                                    .map(p -> new PaymentService.PaymentRequest(
                                            p.getPaymentType(),
                                            p.getAmount(),
                                            p.getTransactionId()))
                                    .toList());
                }
            }

            // Clear the cart
            posService.deleteCart(cartId);

            log.info("Checkout completed for cart: {}, order: {}", cartId, order.getOrderNo());
            return ResponseEntity.ok(ApiResponse.success(order, "Checkout completed successfully"));
        } catch (Exception e) {
            log.error("Error during checkout for cart: {}", cartId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Checkout failed: " + e.getMessage()));
        }
    }

    @DeleteMapping("/cart/{cartId}")
    @Operation(summary = "Clear cart", description = "Remove all items from cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @Parameter(description = "Cart ID") @PathVariable String cartId) {
        try {
            posService.clearCart(cartId);
            return ResponseEntity.ok(ApiResponse.success(null, "Cart cleared successfully"));
        } catch (Exception e) {
            log.error("Error clearing cart: {}", cartId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error clearing cart: " + e.getMessage()));
        }
    }
}
