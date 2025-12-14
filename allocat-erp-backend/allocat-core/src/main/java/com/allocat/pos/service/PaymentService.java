package com.allocat.pos.service;

import com.allocat.pos.entity.Payment;
import com.allocat.pos.entity.SalesOrder;
import com.allocat.pos.enums.PaymentStatus;
import com.allocat.pos.enums.PaymentType;
import com.allocat.pos.repository.PaymentRepository;
import com.allocat.pos.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for processing payments
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SalesOrderRepository salesOrderRepository;

    /**
     * Process a single payment
     */
    @Transactional
    public Payment processPayment(long orderId, PaymentType paymentType, BigDecimal amount, String transactionId) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Validate payment amount
        BigDecimal totalPaid = paymentRepository.findBySalesOrderId(orderId).stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = order.getTotal().subtract(totalPaid);

        if (amount.compareTo(remaining) > 0) {
            throw new RuntimeException("Payment amount exceeds remaining balance. Remaining: " + remaining);
        }

        Payment payment = Payment.builder()
                .salesOrder(order)
                .paymentType(paymentType)
                .amount(amount)
                .transactionId(transactionId)
                .status(PaymentStatus.COMPLETED)
                .processedAt(LocalDateTime.now())
                .build();

        @SuppressWarnings("null") // Spring Data JPA save() never returns null
        Payment savedPayment = paymentRepository.save(payment);

        // Update order payment status
        updateOrderPaymentStatus(order);

        log.info("Processed {} payment of {} for order {}", paymentType, amount, order.getOrderNo());
        return savedPayment;
    }

    /**
     * Process split payments
     */
    @Transactional
    public List<Payment> processSplitPayment(long orderId, List<PaymentRequest> paymentRequests) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Validate total matches order amount
        BigDecimal totalPaymentAmount = paymentRequests.stream()
                .map(PaymentRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPaymentAmount.compareTo(order.getTotal()) != 0) {
            throw new RuntimeException("Split payment total does not match order total. " +
                    "Order: " + order.getTotal() + ", Payments: " + totalPaymentAmount);
        }

        List<Payment> payments = new ArrayList<>();

        for (PaymentRequest request : paymentRequests) {
            Payment payment = Payment.builder()
                    .salesOrder(order)
                    .paymentType(request.getPaymentType())
                    .amount(request.getAmount())
                    .transactionId(request.getTransactionId())
                    .status(PaymentStatus.COMPLETED)
                    .processedAt(LocalDateTime.now())
                    .notes("Split payment")
                    .build();

            @SuppressWarnings("null") // Spring Data JPA save() never returns null
            Payment savedPayment = paymentRepository.save(payment);
            payments.add(savedPayment);
        }

        // Update order payment status
        updateOrderPaymentStatus(order);

        log.info("Processed split payment ({} methods) for order {}",
                paymentRequests.size(), order.getOrderNo());
        return payments;
    }

    /**
     * Process a refund
     */
    @Transactional
    public Payment processRefund(long paymentId, BigDecimal amount, String reason) {
        Payment originalPayment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (originalPayment.getStatus() != PaymentStatus.COMPLETED) {
            throw new RuntimeException("Can only refund completed payments");
        }

        if (amount.compareTo(originalPayment.getAmount()) > 0) {
            throw new RuntimeException("Refund amount cannot exceed original payment amount");
        }

        // Create refund as negative payment
        Payment refund = Payment.builder()
                .salesOrder(originalPayment.getSalesOrder())
                .paymentType(originalPayment.getPaymentType())
                .amount(amount.negate())
                .transactionId("REFUND-" + originalPayment.getTransactionId())
                .status(PaymentStatus.REFUNDED)
                .processedAt(LocalDateTime.now())
                .notes("Refund for payment #" + paymentId + ": " + reason)
                .build();

        @SuppressWarnings("null") // Spring Data JPA save() never returns null
        Payment savedRefund = paymentRepository.save(refund);

        // Update original payment status if fully refunded
        if (amount.compareTo(originalPayment.getAmount()) == 0) {
            originalPayment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(originalPayment);
        }

        log.info("Processed refund of {} for payment {}", amount, paymentId);
        return savedRefund;
    }

    /**
     * Get payment history for an order
     */
    public List<Payment> getPaymentHistory(long orderId) {
        return paymentRepository.findBySalesOrderId(orderId);
    }

    /**
     * Get payment breakdown by type for a store and date
     */
    public Map<PaymentType, BigDecimal> getPaymentBreakdown(long storeId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Object[]> results = paymentRepository.getPaymentBreakdownByType(
                storeId, startOfDay, endOfDay);

        Map<PaymentType, BigDecimal> breakdown = new LinkedHashMap<>();
        for (Object[] result : results) {
            PaymentType type = (PaymentType) result[0];
            BigDecimal amount = (BigDecimal) result[1];
            breakdown.put(type, amount);
        }

        return breakdown;
    }

    /**
     * Get total payments for a store and date range
     */
    public BigDecimal getTotalPayments(long storeId, LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.getTotalPaymentsByStoreAndDateRange(storeId, startDate, endDate);
    }

    /**
     * Update order payment status based on payments received
     */
    private void updateOrderPaymentStatus(SalesOrder order) {
        BigDecimal totalPaid = paymentRepository.findBySalesOrderId(order.getId()).stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPaid.compareTo(order.getTotal()) >= 0) {
            order.setPaymentStatus(com.allocat.pos.enums.PaymentStatus.COMPLETED);
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            order.setPaymentStatus(com.allocat.pos.enums.PaymentStatus.PENDING);
        }

        salesOrderRepository.save(order);
    }

    /**
     * Inner class for split payment requests
     */
    public static class PaymentRequest {
        private PaymentType paymentType;
        private BigDecimal amount;
        private String transactionId;

        public PaymentRequest() {
        }

        public PaymentRequest(PaymentType paymentType, BigDecimal amount) {
            this.paymentType = paymentType;
            this.amount = amount;
        }

        public PaymentRequest(PaymentType paymentType, BigDecimal amount, String transactionId) {
            this.paymentType = paymentType;
            this.amount = amount;
            this.transactionId = transactionId;
        }

        public PaymentType getPaymentType() {
            return paymentType;
        }

        public void setPaymentType(PaymentType paymentType) {
            this.paymentType = paymentType;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }
    }
}
