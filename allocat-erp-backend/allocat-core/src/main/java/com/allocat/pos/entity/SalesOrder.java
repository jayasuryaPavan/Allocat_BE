package com.allocat.pos.entity;

import com.allocat.auth.entity.Customer;
import com.allocat.auth.entity.Store;
import com.allocat.auth.entity.User;
import com.allocat.common.entity.BaseEntity;
import com.allocat.pos.enums.OrderStatus;
import com.allocat.pos.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a sales order/transaction in the POS system
 */
@Entity
@Table(name = "sales_orders", indexes = {
        @Index(name = "idx_sales_orders_order_no", columnList = "order_no"),
        @Index(name = "idx_sales_orders_store_id", columnList = "store_id"),
        @Index(name = "idx_sales_orders_customer_id", columnList = "customer_id"),
        @Index(name = "idx_sales_orders_order_date", columnList = "order_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SalesOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", unique = true, nullable = false, length = 50)
    private String orderNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier_id")
    private User cashier;

    @Column(name = "original_order_id")
    private Long originalOrderId;

    @Column(name = "order_date")
    @Builder.Default
    private LocalDateTime orderDate = LocalDateTime.now();

    @Column(name = "subtotal", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id")
    private Discount discount;

    @Column(name = "total", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.COMPLETED;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SalesOrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    /**
     * Helper method to add an item to the sales order
     */
    public void addItem(SalesOrderItem item) {
        items.add(item);
        item.setSalesOrder(this);
    }

    /**
     * Helper method to remove an item from the sales order
     */
    public void removeItem(SalesOrderItem item) {
        items.remove(item);
        item.setSalesOrder(null);
    }

    /**
     * Helper method to add a payment to the sales order
     */
    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setSalesOrder(this);
    }
}
