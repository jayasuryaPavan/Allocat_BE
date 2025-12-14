package com.allocat.pos.service;

import com.allocat.pos.entity.Receipt;
import com.allocat.pos.entity.SalesOrder;
import com.allocat.pos.repository.ReceiptRepository;
import com.allocat.pos.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service for generating and managing receipts
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final SalesOrderRepository salesOrderRepository;

    /**
     * Generate a receipt for a sales order
     */
    @Transactional
    public Receipt generateReceipt(long orderId, String format) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Check if receipt already exists
        var existingReceipt = receiptRepository.findBySalesOrderId(orderId);
        if (existingReceipt.isPresent()) {
            log.info("Receipt already exists for order: {}", order.getOrderNo());
            return existingReceipt.get();
        }

        String receiptNo = generateReceiptNumber(order);

        Receipt receipt = Receipt.builder()
                .receiptNo(receiptNo)
                .salesOrder(order)
                .format(format != null ? format : "PDF")
                .createdAt(LocalDateTime.now())
                .build();

        @SuppressWarnings("null") // Spring Data JPA save() never returns null
        Receipt saved = receiptRepository.save(receipt);
        log.info("Generated {} receipt: {}", format, receiptNo);
        return saved;
    }

    /**
     * Get receipt by order ID
     */
    public Receipt getReceiptByOrderId(long orderId) {
        return receiptRepository.findBySalesOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Receipt not found for order: " + orderId));
    }

    /**
     * Get receipt by receipt number
     */
    public Receipt getReceiptByReceiptNo(String receiptNo) {
        return receiptRepository.findByReceiptNo(receiptNo)
                .orElseThrow(() -> new RuntimeException("Receipt not found: " + receiptNo));
    }

    /**
     * Generate receipt number
     */
    private String generateReceiptNumber(SalesOrder order) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "RCP-" + order.getStore().getCode() + "-" + timestamp;
    }

    /**
     * TODO: Implement receipt PDF generation
     * This would use a library like iText or Apache PDFBox to generate
     * a formatted PDF receipt with store logo, items, totals, etc.
     */
    public byte[] generateReceiptPDF(long orderId) {
        throw new UnsupportedOperationException("PDF generation not yet implemented");
    }

    /**
     * TODO: Implement thermal receipt printing
     * This would generate ESC/POS commands for thermal printers
     */
    public String generateThermalReceipt(long orderId) {
        throw new UnsupportedOperationException("Thermal receipt printing not yet implemented");
    }
}
