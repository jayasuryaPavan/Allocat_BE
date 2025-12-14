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
import jakarta.annotation.PostConstruct;

import java.util.List;

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

    @PostConstruct
    public void init() {
        log.info("POSController initialized successfully. Endpoints registered at /api/pos/**");
    }

    @PostMapping("/cart")
    @Operation(summary = "Create new cart", description = "Create a new shopping cart for POS transaction")
    public ResponseEntity<ApiResponse<CartDTO>> createCart(@RequestBody CreateCartRequest request) {
        log.info("POST /api/pos/cart - Creating cart for storeId: {}, cashierId: {}",
                request.getStoreId(), request.getCashierId());
        try {
            CartDTO cart = posService.createCart(request.getStoreId(), request.getCashierId());
            log.info("Cart created successfully: {}", cart.getCartId());
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

    @PostMapping("/cart/{cartId}/hold")
    @Operation(summary = "Hold/Park order", description = "Hold the current cart as a parked order")
    public ResponseEntity<ApiResponse<SalesOrder>> holdOrder(
            @Parameter(description = "Cart ID") @PathVariable String cartId,
            @RequestBody(required = false) CheckoutRequest request) {
        try {
            CartDTO cart = posService.getCart(cartId);
            Long customerId = request != null ? request.getCustomerId() : null;
            String notes = request != null ? request.getNotes() : null;

            SalesOrder heldOrder = salesOrderService.holdOrder(cart, customerId, notes);

            // Clear the cart after holding
            posService.deleteCart(cartId);

            return ResponseEntity.ok(ApiResponse.success(heldOrder, "Order held successfully"));
        } catch (Exception e) {
            log.error("Error holding order for cart: {}", cartId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to hold order: " + e.getMessage()));
        }
    }

    @GetMapping("/orders/held")
    @Operation(summary = "Get held orders", description = "Get all held/parked orders for a store")
    public ResponseEntity<ApiResponse<List<SalesOrder>>> getHeldOrders(
            @Parameter(description = "Store ID") @RequestParam Long storeId) {
        try {
            List<SalesOrder> heldOrders = salesOrderService.getHeldOrders(storeId);
            return ResponseEntity.ok(ApiResponse.success(heldOrders));
        } catch (Exception e) {
            log.error("Error retrieving held orders for store: {}", storeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve held orders: " + e.getMessage()));
        }
    }

    @PostMapping("/orders/{orderId}/resume")
    @Operation(summary = "Resume held order", description = "Resume a held order and return its details")
    public ResponseEntity<ApiResponse<SalesOrder>> resumeOrder(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        try {
            SalesOrder resumedOrder = salesOrderService.resumeOrder(orderId);
            return ResponseEntity.ok(ApiResponse.success(resumedOrder, "Order resumed successfully"));
        } catch (Exception e) {
            log.error("Error resuming order: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to resume order: " + e.getMessage()));
        }
    }

    @GetMapping("/orders/search")
    @Operation(summary = "Search order", description = "Search for an order by order number")
    public ResponseEntity<ApiResponse<SalesOrder>> searchOrder(@RequestParam String query) {
        try {
            SalesOrder order = salesOrderService.getOrderByOrderNo(query);
            return ResponseEntity.ok(ApiResponse.success(order));
        } catch (Exception e) {
            log.error("Error searching for order: {}", query, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Order not found: " + e.getMessage()));
        }
    }

    @PostMapping("/returns")
    @Operation(summary = "Process return", description = "Process a return request for an order")
    public ResponseEntity<ApiResponse<SalesOrder>> processReturn(
            @RequestBody com.allocat.pos.dto.ReturnRequest request) {
        try {
            SalesOrder returnOrder = salesOrderService.processReturn(request);
            return ResponseEntity.ok(ApiResponse.success(returnOrder, "Return processed successfully"));
        } catch (Exception e) {
            log.error("Error processing return: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to process return: " + e.getMessage()));
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
