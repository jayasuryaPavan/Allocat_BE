package com.allocat.pos.enums;

/**
 * Enum representing different types of payment methods
 */
public enum PaymentType {
    /**
     * Cash payment
     */
    CASH,

    /**
     * Card payment (Credit/Debit)
     */
    CARD,

    /**
     * Mobile money payment (e.g., M-Pesa, Airtel Money)
     */
    MOBILE_MONEY,

    /**
     * Bank transfer
     */
    BANK_TRANSFER,

    /**
     * Split payment across multiple methods
     */
    SPLIT
}
