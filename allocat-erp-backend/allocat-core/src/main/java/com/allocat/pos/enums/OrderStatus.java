package com.allocat.pos.enums;

/**
 * Enum representing the status of a sales order
 */
public enum OrderStatus {
    /**
     * Order is in draft state (not yet finalized)
     */
    DRAFT,

    /**
     * Order is pending confirmation
     */
    PENDING,

    /**
     * Order has been confirmed
     */
    CONFIRMED,

    /**
     * Order is being processed
     */
    PROCESSING,

    /**
     * Order has been completed
     */
    COMPLETED,

    /**
     * Order has been cancelled
     */
    CANCELLED,

    /**
     * Order has been returned
     */
    RETURNED
}
