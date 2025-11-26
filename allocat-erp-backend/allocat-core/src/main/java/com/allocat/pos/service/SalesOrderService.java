package com.allocat.pos.service;

import com.allocat.auth.entity.Customer;
import com.allocat.auth.entity.Store;
import com.allocat.auth.entity.User;
import com.allocat.auth.repository.CustomerRepository;
import com.allocat.auth.repository.StoreRepository;
import com.allocat.auth.repository.UserRepository;
import com.allocat.inventory.entity.Product;
import com.allocat.inventory.repository.ProductRepository;
import com.allocat.inventory.service.InventoryService;
import com.allocat.pos.dto.CartDTO;
import com.allocat.pos.dto.CartItemDTO;
import com.allocat.pos.entity.Discount;
import com.allocat.pos.entity.SalesOrder;
import com.allocat.pos.entity.SalesOrderItem;
import com.allocat.pos.enums.OrderStatus;
import com.allocat.pos.repository.DiscountRepository;
import com.allocat.pos.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for managing sales orders
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final DiscountRepository discountRepository;
    private final InventoryService inventoryService;

    /**
     * Create a sales order from a cart
     */
    @Transactional
    public SalesOrder createSalesOrderFromCart(CartDTO cart, Long customerId, String notes) {
        // Fetch entities
        Store store = storeRepository.findById(cart.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found"));
        User cashier = userRepository.findById(cart.getCashierId())
                .orElseThrow(() -> new RuntimeException("Cashier not found"));

        Customer customer = null;
        if (customerId != null) {
            customer = customerRepository.findById(customerId).orElse(null);
        }

        Discount discount = null;
        if (cart.getDiscount() != null) {
            discount = discountRepository.findById(cart.getDiscount().getId()).orElse(null);
        }

        // Generate order number
        String orderNo = generateOrderNumber(store.getId());

        // Create sales order
        SalesOrder salesOrder = SalesOrder.builder()
                .orderNo(orderNo)
                .store(store)
                .customer(customer)
                .cashier(cashier)
                .orderDate(LocalDateTime.now())
                .subtotal(cart.getSubtotal())
                .taxAmount(cart.getTaxAmount())
                .discountAmount(cart.getDiscountAmount())
                .discount(discount)
                .total(cart.getTotal())
                .status(OrderStatus.COMPLETED)
                .notes(notes)
                .build();

        // Add items
        for (CartItemDTO cartItem : cart.getItems()) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + cartItem.getProductId()));

            SalesOrderItem orderItem = SalesOrderItem.builder()
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
                    .discount(cartItem.getDiscount())
                    .taxRate(cartItem.getTaxRate())
                    .taxAmount(cartItem.getTaxAmount())
                    .total(cartItem.getTotal())
                    .build();

            salesOrder.addItem(orderItem);

            // Deduct inventory
            try {
                inventoryService.updateInventoryQuantity(
                        product.getId(),
                        -cartItem.getQuantity(),
                        "POS-" + cashier.getId(),
                        "Sale: " + orderNo);
            } catch (Exception e) {
                log.error("Failed to update inventory for product {}: {}", product.getId(), e.getMessage());
                throw new RuntimeException("Failed to update inventory: " + e.getMessage());
            }
        }

        // Increment discount usage if applied
        if (discount != null) {
            discountRepository.incrementUsageCount(discount.getId());
        }

        SalesOrder savedOrder = salesOrderRepository.save(salesOrder);
        log.info("Created sales order: {}", orderNo);
        return savedOrder;
    }

    /**
     * Get sales order by ID
     */
    public SalesOrder getOrderById(Long id) {
        return salesOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sales order not found: " + id));
    }

    /**
     * Get sales order by order number
     */
    public SalesOrder getOrderByOrderNo(String orderNo) {
        return salesOrderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new RuntimeException("Sales order not found: " + orderNo));
    }

    /**
     * Get orders by store and date range
     */
    public Page<SalesOrder> getOrdersByStoreAndDateRange(
            Long storeId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {
        return salesOrderRepository.findByStoreAndDateRange(storeId, startDate, endDate, pageable);
    }

    /**
     * Get orders by customer
     */
    public List<SalesOrder> getOrdersByCustomer(Long customerId) {
        return salesOrderRepository.findByCustomerId(customerId);
    }

    /**
     * Cancel an order
     */
    @Transactional
    public SalesOrder cancelOrder(Long orderId, String reason) {
        SalesOrder order = getOrderById(orderId);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order is already cancelled");
        }

        if (order.getStatus() == OrderStatus.RETURNED) {
            throw new RuntimeException("Cannot cancel a returned order");
        }

        // Return items to inventory
        for (SalesOrderItem item : order.getItems()) {
            try {
                inventoryService.updateInventoryQuantity(
                        item.getProduct().getId(),
                        item.getQuantity(),
                        "SYSTEM",
                        "Order cancelled: " + order.getOrderNo() + " - " + reason);
            } catch (Exception e) {
                log.error("Failed to restore inventory for product {}: {}",
                        item.getProduct().getId(), e.getMessage());
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setNotes((order.getNotes() != null ? order.getNotes() + "\n" : "") +
                "Cancelled: " + reason);

        SalesOrder cancelled = salesOrderRepository.save(order);
        log.info("Cancelled order: {}", order.getOrderNo());
        return cancelled;
    }

    /**
     * Get total sales for a store in a date range
     */
    public BigDecimal getTotalSales(Long storeId, LocalDateTime startDate, LocalDateTime endDate) {
        return salesOrderRepository.getTotalSalesByStoreAndDateRange(storeId, startDate, endDate);
    }

    /**
     * Get order count for a store in a date range
     */
    public Long getOrderCount(Long storeId, LocalDateTime startDate, LocalDateTime endDate) {
        return salesOrderRepository.countOrdersByStoreAndDateRange(storeId, startDate, endDate);
    }

    /**
     * Hold/Park an order
     */
    @Transactional
    public SalesOrder holdOrder(CartDTO cart, Long customerId, String notes) {
        SalesOrder order = createSalesOrderFromCart(cart, customerId, notes);
        order.setStatus(OrderStatus.HELD);
        return salesOrderRepository.save(order);
    }

    /**
     * Resume a held order
     * This cancels the held order (restoring inventory) and returns the order
     * details
     * so they can be added back to the cart
     */
    @Transactional
    public SalesOrder resumeOrder(Long orderId) {
        SalesOrder order = getOrderById(orderId);

        if (order.getStatus() != OrderStatus.HELD) {
            throw new RuntimeException("Order is not in HELD status");
        }

        // Cancel the order to release inventory
        // We don't use the standard cancelOrder because we want a specific reason
        // and we might want to handle it slightly differently, but standard is fine for
        // now
        return cancelOrder(orderId, "Resumed to cart");
    }

    /**
     * Get all held orders for a store
     */
    public List<SalesOrder> getHeldOrders(Long storeId) {
        return salesOrderRepository.findByStoreIdAndStatus(storeId, OrderStatus.HELD);
    }

    private String generateOrderNumber(Long storeId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String storePrefix = String.format("S%03d", storeId);
        return storePrefix + "-" + timestamp;
    }

    /**
     * Process a return request
     */
    @Transactional
    public SalesOrder processReturn(com.allocat.pos.dto.ReturnRequest request) {
        SalesOrder originalOrder = getOrderById(request.getOriginalOrderId());

        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found"));
        User cashier = userRepository.findById(request.getCashierId())
                .orElseThrow(() -> new RuntimeException("Cashier not found"));

        // Create return order
        SalesOrder returnOrder = SalesOrder.builder()
                .orderNo("RET-" + generateOrderNumber(store.getId()))
                .store(store)
                .customer(originalOrder.getCustomer())
                .cashier(cashier)
                .originalOrderId(originalOrder.getId())
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.RETURNED)
                .paymentStatus(com.allocat.pos.enums.PaymentStatus.REFUNDED)
                .notes("Return for Order: " + originalOrder.getOrderNo() + ". Reason: " + request.getReason())
                .build();

        BigDecimal totalRefund = BigDecimal.ZERO;
        BigDecimal totalTaxRefund = BigDecimal.ZERO;
        BigDecimal totalSubtotalRefund = BigDecimal.ZERO;

        for (com.allocat.pos.dto.ReturnRequest.ReturnItemDTO itemDTO : request.getItems()) {
            SalesOrderItem originalItem = originalOrder.getItems().stream()
                    .filter(i -> i.getProduct().getId().equals(itemDTO.getProductId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(
                            "Product not found in original order: " + itemDTO.getProductId()));

            if (itemDTO.getQuantity() > originalItem.getQuantity()) {
                throw new RuntimeException("Cannot return more than purchased quantity for product: "
                        + originalItem.getProduct().getName());
            }

            // Create return item (negative values)
            BigDecimal unitPrice = originalItem.getUnitPrice();
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            BigDecimal taxAmount = itemTotal.multiply(BigDecimal.valueOf(0.15)); // Assuming 15% tax, should ideally
                                                                                 // come from original item rate

            SalesOrderItem returnItem = SalesOrderItem.builder()
                    .product(originalItem.getProduct())
                    .quantity(-itemDTO.getQuantity()) // Negative quantity
                    .unitPrice(unitPrice)
                    .discount(BigDecimal.ZERO) // Simplified: no discount reversal for now
                    .taxRate(originalItem.getTaxRate())
                    .taxAmount(taxAmount.negate())
                    .total(itemTotal.negate())
                    .build();

            returnOrder.addItem(returnItem);

            totalSubtotalRefund = totalSubtotalRefund.add(itemTotal);
            totalTaxRefund = totalTaxRefund.add(taxAmount);
            totalRefund = totalRefund.add(itemTotal.add(taxAmount));

            // Restore inventory
            try {
                inventoryService.updateInventoryQuantity(
                        originalItem.getProduct().getId(),
                        itemDTO.getQuantity(), // Positive quantity to add back
                        "POS-" + cashier.getId(),
                        "Return: " + returnOrder.getOrderNo());
            } catch (Exception e) {
                log.error("Failed to restore inventory for product {}: {}", originalItem.getProduct().getId(),
                        e.getMessage());
                throw new RuntimeException("Failed to restore inventory: " + e.getMessage());
            }
        }

        returnOrder.setSubtotal(totalSubtotalRefund.negate());
        returnOrder.setTaxAmount(totalTaxRefund.negate());
        returnOrder.setTotal(totalRefund.negate());

        return salesOrderRepository.save(returnOrder);
    }
}
