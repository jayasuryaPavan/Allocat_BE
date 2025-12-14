package com.allocat.pos.enums;

/**
 * Enum representing the status of a payment transaction
 */
public enum PaymentStatus {
    /**
     * Payment is pending processing
     */
    PENDING,

    /**
     * Payment has been successfully completed
     */
    COMPLETED,

    /**
     * Payment processing failed
     */
    FAILED,

    /**
     * Payment has been refunded
     */
    REFUNDED
}
