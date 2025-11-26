package com.allocat.api.controller;

import com.allocat.common.dto.ApiResponse;
import com.allocat.pos.entity.Receipt;
import com.allocat.pos.service.ReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Receipt generation and management
 */
@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Receipts", description = "APIs for receipt generation and delivery")
public class ReceiptController {

    private final ReceiptService receiptService;

    @GetMapping("/{orderId}")
    @Operation(summary = "Get receipt", description = "Retrieve receipt for a sales order")
    public ResponseEntity<ApiResponse<Receipt>> getReceipt(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        try {
            Receipt receipt = receiptService.getReceiptByOrderId(orderId);
            return ResponseEntity.ok(ApiResponse.success(receipt));
        } catch (Exception e) {
            log.error("Error retrieving receipt for order: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Receipt not found: " + e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/generate")
    @Operation(summary = "Generate receipt", description = "Generate a receipt for a sales order")
    public ResponseEntity<ApiResponse<Receipt>> generateReceipt(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Parameter(description = "Receipt format (PDF/THERMAL)") @RequestParam(defaultValue = "PDF") String format) {
        try {
            Receipt receipt = receiptService.generateReceipt(orderId, format);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(receipt, "Receipt generated successfully"));
        } catch (Exception e) {
            log.error("Error generating receipt for order: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error generating receipt: " + e.getMessage()));
        }
    }

    @GetMapping("/{orderId}/pdf")
    @Operation(summary = "Get receipt PDF", description = "Download receipt as PDF (not yet implemented)")
    public ResponseEntity<byte[]> getReceiptPDF(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        try {
            // TODO: Implement PDF generation
            byte[] pdfBytes = receiptService.generateReceiptPDF(orderId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "receipt-" + orderId + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (UnsupportedOperationException e) {
            log.warn("PDF generation not yet implemented");
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        } catch (Exception e) {
            log.error("Error generating PDF for order: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{orderId}/thermal")
    @Operation(summary = "Get thermal receipt", description = "Get thermal printer commands (not yet implemented)")
    public ResponseEntity<ApiResponse<String>> getThermalReceipt(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        try {
            String thermalCommands = receiptService.generateThermalReceipt(orderId);
            return ResponseEntity.ok(ApiResponse.success(thermalCommands, "Thermal receipt generated"));
        } catch (UnsupportedOperationException e) {
            log.warn("Thermal printing not yet implemented");
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(ApiResponse.error("Thermal printing not yet implemented"));
        } catch (Exception e) {
            log.error("Error generating thermal receipt for order: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error generating thermal receipt: " + e.getMessage()));
        }
    }
}
