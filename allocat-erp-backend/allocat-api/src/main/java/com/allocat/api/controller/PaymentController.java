package com.allocat.api.controller;

import com.allocat.api.dto.pos.PaymentRequest;
import com.allocat.common.dto.ApiResponse;
import com.allocat.pos.entity.Payment;
import com.allocat.pos.enums.PaymentType;
import com.allocat.pos.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Payment processing
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "APIs for payment processing and reconciliation")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Process payment", description = "Process a payment for a sales order")
    public ResponseEntity<ApiResponse<Payment>> processPayment(@RequestBody PaymentRequest request) {
        try {
            Payment payment = paymentService.processPayment(
                    request.getOrderId(),
                    request.getPaymentType(),
                    request.getAmount(),
                    request.getTransactionId());
            return ResponseEntity.ok(ApiResponse.success(payment, "Payment processed successfully"));
        } catch (Exception e) {
            log.error("Error processing payment", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Payment processing failed: " + e.getMessage()));
        }
    }

    @PostMapping("/split")
    @Operation(summary = "Process split payment", description = "Process multiple payment methods for one order")
    public ResponseEntity<ApiResponse<List<Payment>>> processSplitPayment(
            @RequestBody SplitPaymentRequestDTO request) {
        try {
            List<Payment> payments = paymentService.processSplitPayment(
                    request.getOrderId(),
                    request.getPayments().stream()
                            .map(p -> new PaymentService.PaymentRequest(
                                    p.getPaymentType(),
                                    p.getAmount(),
                                    p.getTransactionId()))
                            .toList());
            return ResponseEntity.ok(ApiResponse.success(payments, "Split payment processed successfully"));
        } catch (Exception e) {
            log.error("Error processing split payment", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Split payment failed: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Process refund", description = "Refund a payment partially or fully")
    public ResponseEntity<ApiResponse<Payment>> processRefund(
            @Parameter(description = "Payment ID") @PathVariable Long id,
            @RequestBody RefundRequest request) {
        try {
            Payment refund = paymentService.processRefund(id, request.getAmount(), request.getReason());
            return ResponseEntity.ok(ApiResponse.success(refund, "Refund processed successfully"));
        } catch (Exception e) {
            log.error("Error processing refund for payment: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Refund failed: " + e.getMessage()));
        }
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment history", description = "Retrieve all payments for a sales order")
    public ResponseEntity<ApiResponse<List<Payment>>> getPaymentHistory(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        try {
            List<Payment> payments = paymentService.getPaymentHistory(orderId);
            return ResponseEntity.ok(ApiResponse.success(payments, "Payment history retrieved"));
        } catch (Exception e) {
            log.error("Error retrieving payment history for order: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving payment history: " + e.getMessage()));
        }
    }

    @GetMapping("/reconcile")
    @Operation(summary = "Get payment reconciliation", description = "Get payment breakdown by type for a date")
    public ResponseEntity<ApiResponse<Map<PaymentType, BigDecimal>>> getReconciliation(
            @Parameter(description = "Store ID") @RequestParam Long storeId,
            @Parameter(description = "Date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            Map<PaymentType, BigDecimal> breakdown = paymentService.getPaymentBreakdown(storeId, date);

            // Calculate total
            BigDecimal total = breakdown.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            log.info("Reconciliation for store {} on {}: Total = {}", storeId, date, total);

            return ResponseEntity.ok(ApiResponse.success(breakdown,
                    "Reconciliation for " + date + ": Total = " + total));
        } catch (Exception e) {
            log.error("Error getting payment reconciliation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error getting reconciliation: " + e.getMessage()));
        }
    }

    @GetMapping("/total")
    @Operation(summary = "Get total payments", description = "Get total payments for a store and date range")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalPayments(
            @Parameter(description = "Store ID") @RequestParam Long storeId,
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            BigDecimal total = paymentService.getTotalPayments(storeId, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success(total, "Total payments: " + total));
        } catch (Exception e) {
            log.error("Error calculating total payments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error calculating total: " + e.getMessage()));
        }
    }

    // DTOs for request bodies
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SplitPaymentRequestDTO {
        private Long orderId;
        private List<PaymentRequest> payments;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefundRequest {
        private BigDecimal amount;
        private String reason;
    }
}
