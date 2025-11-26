package com.allocat.pos.service;

import com.allocat.auth.repository.StoreRepository;
import com.allocat.auth.repository.UserRepository;
import com.allocat.inventory.entity.Product;
import com.allocat.inventory.repository.ProductRepository;
import com.allocat.inventory.service.InventoryService;
import com.allocat.pos.dto.CartDTO;
import com.allocat.pos.dto.CartItemDTO;
import com.allocat.pos.dto.DiscountDTO;
import com.allocat.pos.entity.Discount;
import com.allocat.pos.repository.DiscountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing POS shopping carts
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class POSService {

    private final ProductRepository productRepository;
    private final DiscountRepository discountRepository;
    private final InventoryService inventoryService;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    // In-memory cart storage (consider Redis for production/multi-instance)
    private final Map<String, CartDTO> activeCarts = new ConcurrentHashMap<>();

    // Default tax rate (should be configurable per store)
    private static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("0.15"); // 15%

    /**
     * Create a new shopping cart
     */
    public CartDTO createCart(Long storeId, Long cashierId) {
        // Validate store and cashier exist
        if (!storeRepository.existsById(storeId)) {
            throw new RuntimeException("Store not found");
        }
        if (!userRepository.existsById(cashierId)) {
            throw new RuntimeException("Cashier not found");
        }

        String cartId = UUID.randomUUID().toString();
        CartDTO cart = CartDTO.builder()
                .cartId(cartId)
                .storeId(storeId)
                .cashierId(cashierId)
                .items(new ArrayList<>())
                .subtotal(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        activeCarts.put(cartId, cart);
        log.info("Created cart: {} for store: {} cashier: {}", cartId, storeId, cashierId);
        return cart;
    }

    /**
     * Get an existing cart
     */
    public CartDTO getCart(String cartId) {
        CartDTO cart = activeCarts.get(cartId);
        if (cart == null) {
            throw new RuntimeException("Cart not found: " + cartId);
        }
        return cart;
    }

    /**
     * Add item to cart by product ID
     */
    public CartDTO addItemToCart(String cartId, Long productId, Integer quantity) {
        CartDTO cart = getCart(cartId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        if (!product.getIsActive()) {
            throw new RuntimeException("Product is not active: " + product.getName());
        }

        // Check inventory availability
        var inventory = inventoryService.getInventoryByProductId(productId);
        if (inventory.isEmpty() || inventory.get().getAvailableQuantity() < quantity) {
            throw new RuntimeException("Insufficient inventory for product: " + product.getName());
        }

        // Check if product already in cart
        Optional<CartItemDTO> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update quantity of existing item
            CartItemDTO item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            calculateItemTotal(item);
        } else {
            // Add new item
            String itemId = UUID.randomUUID().toString();
            CartItemDTO newItem = CartItemDTO.builder()
                    .itemId(itemId)
                    .productId(product.getId())
                    .productName(product.getName())
                    .sku(product.getSku())
                    .barcode(product.getBarcode())
                    .quantity(quantity)
                    .unitPrice(product.getUnitPrice())
                    .taxRate(DEFAULT_TAX_RATE)
                    .discount(BigDecimal.ZERO)
                    .build();

            calculateItemTotal(newItem);
            cart.getItems().add(newItem);
        }

        recalculateCart(cart);
        log.info("Added {} x {} to cart {}", quantity, product.getName(), cartId);
        return cart;
    }

    /**
     * Add item to cart by barcode
     */
    public CartDTO addItemByBarcode(String cartId, String barcode, Integer quantity) {
        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new RuntimeException("Product not found with barcode: " + barcode));
        return addItemToCart(cartId, product.getId(), quantity);
    }

    /**
     * Update cart item quantity
     */
    public CartDTO updateCartItem(String cartId, String itemId, Integer quantity) {
        CartDTO cart = getCart(cartId);

        CartItemDTO item = cart.getItems().stream()
                .filter(i -> i.getItemId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in cart: " + itemId));

        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        // Check inventory availability
        var inventory = inventoryService.getInventoryByProductId(item.getProductId());
        if (inventory.isEmpty() || inventory.get().getAvailableQuantity() < quantity) {
            throw new RuntimeException("Insufficient inventory for product: " + item.getProductName());
        }

        item.setQuantity(quantity);
        calculateItemTotal(item);
        recalculateCart(cart);

        log.info("Updated item {} quantity to {} in cart {}", itemId, quantity, cartId);
        return cart;
    }

    /**
     * Remove item from cart
     */
    public CartDTO removeItemFromCart(String cartId, String itemId) {
        CartDTO cart = getCart(cartId);

        cart.getItems().removeIf(item -> item.getItemId().equals(itemId));
        recalculateCart(cart);

        log.info("Removed item {} from cart {}", itemId, cartId);
        return cart;
    }

    /**
     * Apply discount to cart
     */
    public CartDTO applyDiscount(String cartId, String discountCode) {
        CartDTO cart = getCart(cartId);

        Discount discount = discountRepository.findByCode(discountCode)
                .orElseThrow(() -> new RuntimeException("Discount code not found: " + discountCode));

        // Validate discount
        String validationMessage = validateDiscount(discount, cart.getSubtotal());
        if (validationMessage != null) {
            throw new RuntimeException(validationMessage);
        }

        // Map to DTO
        DiscountDTO discountDTO = mapDiscountToDTO(discount);
        cart.setDiscount(discountDTO);

        recalculateCart(cart);
        log.info("Applied discount {} to cart {}", discountCode, cartId);
        return cart;
    }

    /**
     * Remove discount from cart
     */
    public CartDTO removeDiscount(String cartId) {
        CartDTO cart = getCart(cartId);
        cart.setDiscount(null);
        recalculateCart(cart);

        log.info("Removed discount from cart {}", cartId);
        return cart;
    }

    /**
     * Clear all items from cart
     */
    public void clearCart(String cartId) {
        CartDTO cart = getCart(cartId);
        cart.getItems().clear();
        cart.setDiscount(null);
        recalculateCart(cart);

        log.info("Cleared cart {}", cartId);
    }

    /**
     * Delete cart completely
     */
    public void deleteCart(String cartId) {
        activeCarts.remove(cartId);
        log.info("Deleted cart {}", cartId);
    }

    /**
     * Calculate total for a single cart item
     */
    private void calculateItemTotal(CartItemDTO item) {
        BigDecimal itemSubtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        itemSubtotal = itemSubtotal.subtract(item.getDiscount());

        BigDecimal taxAmount = itemSubtotal.multiply(item.getTaxRate())
                .setScale(2, RoundingMode.HALF_UP);
        item.setTaxAmount(taxAmount);

        BigDecimal total = itemSubtotal.add(taxAmount);
        item.setTotal(total);
    }

    /**
     * Recalculate cart totals
     */
    private void recalculateCart(CartDTO cart) {
        BigDecimal subtotal = cart.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxAmount = cart.getItems().stream()
                .map(CartItemDTO::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (cart.getDiscount() != null) {
            discountAmount = calculateDiscountAmount(cart.getDiscount(), subtotal);
        }

        BigDecimal total = subtotal.add(taxAmount).subtract(discountAmount);

        cart.setSubtotal(subtotal);
        cart.setTaxAmount(taxAmount);
        cart.setDiscountAmount(discountAmount);
        cart.setTotal(total.max(BigDecimal.ZERO)); // Ensure total is not negative
    }

    /**
     * Calculate discount amount based on discount type
     */
    private BigDecimal calculateDiscountAmount(DiscountDTO discount, BigDecimal subtotal) {
        BigDecimal discountAmount = BigDecimal.ZERO;

        switch (discount.getType()) {
            case PERCENTAGE:
                discountAmount = subtotal.multiply(discount.getValue())
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                break;
            case FIXED_AMOUNT:
                discountAmount = discount.getValue();
                break;
            case BUY_X_GET_Y:
                // TODO: Implement Buy X Get Y logic
                discountAmount = BigDecimal.ZERO;
                break;
        }

        // Apply max discount limit if set
        if (discount.getMaxDiscountAmount() != null &&
                discountAmount.compareTo(discount.getMaxDiscountAmount()) > 0) {
            discountAmount = discount.getMaxDiscountAmount();
        }

        // Ensure discount doesn't exceed subtotal
        if (discountAmount.compareTo(subtotal) > 0) {
            discountAmount = subtotal;
        }

        return discountAmount;
    }

    /**
     * Validate if a discount can be applied
     */
    private String validateDiscount(Discount discount, BigDecimal cartTotal) {
        if (!discount.getIsActive()) {
            return "Discount is not active";
        }

        LocalDateTime now = LocalDateTime.now();
        if (discount.getValidFrom() != null && now.toLocalDate().isBefore(discount.getValidFrom())) {
            return "Discount is not yet valid";
        }

        if (discount.getValidTo() != null && now.toLocalDate().isAfter(discount.getValidTo())) {
            return "Discount has expired";
        }

        if (discount.getMinPurchaseAmount() != null &&
                cartTotal.compareTo(discount.getMinPurchaseAmount()) < 0) {
            return "Minimum purchase amount not met. Required: " + discount.getMinPurchaseAmount();
        }

        if (discount.getMaxUsageCount() != null &&
                discount.getCurrentUsageCount() >= discount.getMaxUsageCount()) {
            return "Discount usage limit reached";
        }

        return null; // Valid
    }

    /**
     * Map Discount entity to DTO
     */
    private DiscountDTO mapDiscountToDTO(Discount discount) {
        return DiscountDTO.builder()
                .id(discount.getId())
                .code(discount.getCode())
                .name(discount.getName())
                .type(discount.getType())
                .value(discount.getValue())
                .minPurchaseAmount(discount.getMinPurchaseAmount())
                .maxDiscountAmount(discount.getMaxDiscountAmount())
                .validFrom(discount.getValidFrom())
                .validTo(discount.getValidTo())
                .maxUsageCount(discount.getMaxUsageCount())
                .currentUsageCount(discount.getCurrentUsageCount())
                .isActive(discount.getIsActive())
                .build();
    }
}
