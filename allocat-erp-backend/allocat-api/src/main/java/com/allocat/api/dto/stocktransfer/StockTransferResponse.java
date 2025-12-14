package com.allocat.api.dto.stocktransfer;

import com.allocat.inventory.entity.StockTransfer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransferResponse {
    private Long id;
    private String transferNo;
    private Long fromStoreId;
    private String fromStoreName;
    private String fromStoreCode;
    private Long toStoreId;
    private String toStoreName;
    private String toStoreCode;
    private Long fromWarehouseId;
    private String fromWarehouseName;
    private String fromWarehouseCode;
    private Long toWarehouseId;
    private String toWarehouseName;
    private String toWarehouseCode;
    private String status;
    private String transferType;
    private String priority;
    private Long requestedById;
    private String requestedByName;
    private Long approvedById;
    private String approvedByName;
    private Long receivedById;
    private String receivedByName;
    private LocalDateTime transferDate;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime actualDeliveryDate;
    private LocalDateTime receivedDate;
    private String shippingMethod;
    private String trackingNumber;
    private String notes;
    private List<StockTransferItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static StockTransferResponse fromEntity(StockTransfer transfer) {
        StockTransferResponse response = StockTransferResponse.builder()
                .id(transfer.getId())
                .transferNo(transfer.getTransferNo())
                .status(transfer.getStatus() != null ? transfer.getStatus().name() : null)
                .transferType(transfer.getTransferType() != null ? transfer.getTransferType().name() : null)
                .priority(transfer.getPriority() != null ? transfer.getPriority().name() : null)
                .transferDate(transfer.getTransferDate())
                .estimatedDeliveryDate(transfer.getEstimatedDeliveryDate())
                .actualDeliveryDate(transfer.getActualDeliveryDate())
                .receivedDate(transfer.getReceivedDate())
                .shippingMethod(transfer.getShippingMethod())
                .trackingNumber(transfer.getTrackingNumber())
                .notes(transfer.getNotes())
                .createdAt(transfer.getCreatedAt())
                .updatedAt(transfer.getUpdatedAt())
                .build();

        if (transfer.getFromStore() != null) {
            response.setFromStoreId(transfer.getFromStore().getId());
            response.setFromStoreName(transfer.getFromStore().getName());
            response.setFromStoreCode(transfer.getFromStore().getCode());
        }

        if (transfer.getToStore() != null) {
            response.setToStoreId(transfer.getToStore().getId());
            response.setToStoreName(transfer.getToStore().getName());
            response.setToStoreCode(transfer.getToStore().getCode());
        }

        if (transfer.getFromWarehouse() != null) {
            response.setFromWarehouseId(transfer.getFromWarehouse().getId());
            response.setFromWarehouseName(transfer.getFromWarehouse().getName());
            response.setFromWarehouseCode(transfer.getFromWarehouse().getCode());
        }

        if (transfer.getToWarehouse() != null) {
            response.setToWarehouseId(transfer.getToWarehouse().getId());
            response.setToWarehouseName(transfer.getToWarehouse().getName());
            response.setToWarehouseCode(transfer.getToWarehouse().getCode());
        }

        if (transfer.getRequestedBy() != null) {
            response.setRequestedById(transfer.getRequestedBy().getId());
            if (transfer.getRequestedBy().getFirstName() != null || transfer.getRequestedBy().getLastName() != null) {
                response.setRequestedByName((
                    (transfer.getRequestedBy().getFirstName() != null ? transfer.getRequestedBy().getFirstName() : "") +
                    " " +
                    (transfer.getRequestedBy().getLastName() != null ? transfer.getRequestedBy().getLastName() : "")
                ).trim());
            }
        }

        if (transfer.getApprovedBy() != null) {
            response.setApprovedById(transfer.getApprovedBy().getId());
            if (transfer.getApprovedBy().getFirstName() != null || transfer.getApprovedBy().getLastName() != null) {
                response.setApprovedByName((
                    (transfer.getApprovedBy().getFirstName() != null ? transfer.getApprovedBy().getFirstName() : "") +
                    " " +
                    (transfer.getApprovedBy().getLastName() != null ? transfer.getApprovedBy().getLastName() : "")
                ).trim());
            }
        }

        if (transfer.getReceivedBy() != null) {
            response.setReceivedById(transfer.getReceivedBy().getId());
            if (transfer.getReceivedBy().getFirstName() != null || transfer.getReceivedBy().getLastName() != null) {
                response.setReceivedByName((
                    (transfer.getReceivedBy().getFirstName() != null ? transfer.getReceivedBy().getFirstName() : "") +
                    " " +
                    (transfer.getReceivedBy().getLastName() != null ? transfer.getReceivedBy().getLastName() : "")
                ).trim());
            }
        }

        if (transfer.getItems() != null) {
            response.setItems(transfer.getItems().stream()
                    .map(StockTransferItemResponse::fromEntity)
                    .collect(Collectors.toList()));
        }

        return response;
    }
}
