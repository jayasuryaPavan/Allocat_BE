package com.allocat.pos.service;

import com.allocat.inventory.entity.Inventory;
import com.allocat.inventory.repository.InventoryRepository;
import com.allocat.pos.dto.AnalyticsDTO;
import com.allocat.pos.entity.SalesOrder;
import com.allocat.pos.entity.SalesOrderItem;
import com.allocat.pos.enums.OrderStatus;
import com.allocat.pos.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final SalesOrderRepository salesOrderRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * Get sales summary for a specific date range
     */
    public AnalyticsDTO.SalesSummary getSalesSummary(LocalDate startDate, LocalDate endDate, Long storeId) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<SalesOrder> orders = salesOrderRepository.findByOrderDateBetweenAndStoreIdAndStatus(
                start, end, storeId, OrderStatus.COMPLETED);

        BigDecimal totalSales = orders.stream()
                .map(SalesOrder::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxCollected = orders.stream()
                .map(SalesOrder::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountsGiven = orders.stream()
                .map(SalesOrder::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long transactionCount = (long) orders.size();
        BigDecimal averageTicket = transactionCount > 0
                ? totalSales.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return AnalyticsDTO.SalesSummary.builder()
                .date(startDate)
                .totalSales(totalSales)
                .transactionCount(transactionCount)
                .averageTicket(averageTicket)
                .taxCollected(taxCollected)
                .discountsGiven(discountsGiven)
                .build();
    }

    /**
     * Get sales trends over a period
     */
    public AnalyticsDTO.SalesTrendResponse getSalesTrends(LocalDate startDate, LocalDate endDate, Long storeId,
            String periodType) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<SalesOrder> orders = salesOrderRepository.findByOrderDateBetweenAndStoreIdAndStatus(
                start, end, storeId, OrderStatus.COMPLETED);

        List<AnalyticsDTO.TrendData> trends = new ArrayList<>();

        if ("daily".equals(periodType)) {
            // Group by day
            Map<LocalDate, List<SalesOrder>> ordersByDay = orders.stream()
                    .collect(Collectors.groupingBy(order -> order.getOrderDate().toLocalDate()));

            ordersByDay.forEach((date, dayOrders) -> {
                BigDecimal daySales = dayOrders.stream()
                        .map(SalesOrder::getTotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                trends.add(AnalyticsDTO.TrendData.builder()
                        .period(date.toString())
                        .sales(daySales)
                        .transactions((long) dayOrders.size())
                        .build());
            });
        } else if ("weekly".equals(periodType)) {
            // Group by week
            Map<String, List<SalesOrder>> ordersByWeek = orders.stream()
                    .collect(Collectors.groupingBy(order -> {
                        LocalDate date = order.getOrderDate().toLocalDate();
                        return date.getYear() + "-W" + date.format(DateTimeFormatter.ofPattern("ww"));
                    }));

            ordersByWeek.forEach((week, weekOrders) -> {
                BigDecimal weekSales = weekOrders.stream()
                        .map(SalesOrder::getTotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                trends.add(AnalyticsDTO.TrendData.builder()
                        .period(week)
                        .sales(weekSales)
                        .transactions((long) weekOrders.size())
                        .build());
            });
        } else if ("monthly".equals(periodType)) {
            // Group by month
            Map<String, List<SalesOrder>> ordersByMonth = orders.stream()
                    .collect(Collectors.groupingBy(order -> {
                        LocalDate date = order.getOrderDate().toLocalDate();
                        return date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                    }));

            ordersByMonth.forEach((month, monthOrders) -> {
                BigDecimal monthSales = monthOrders.stream()
                        .map(SalesOrder::getTotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                trends.add(AnalyticsDTO.TrendData.builder()
                        .period(month)
                        .sales(monthSales)
                        .transactions((long) monthOrders.size())
                        .build());
            });
        }

        return AnalyticsDTO.SalesTrendResponse.builder()
                .trends(trends)
                .periodType(periodType)
                .build();
    }

    /**
     * Get cashier performance metrics
     */
    public List<AnalyticsDTO.CashierPerformance> getCashierPerformance(LocalDate startDate, LocalDate endDate,
            Long storeId) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<SalesOrder> orders = salesOrderRepository.findByOrderDateBetweenAndStoreIdAndStatus(
                start, end, storeId, OrderStatus.COMPLETED);

        Map<Long, List<SalesOrder>> ordersByCashier = orders.stream()
                .collect(Collectors.groupingBy(order -> order.getCashier().getId()));

        List<AnalyticsDTO.CashierPerformance> performances = new ArrayList<>();

        ordersByCashier.forEach((cashierId, cashierOrders) -> {
            BigDecimal totalSales = cashierOrders.stream()
                    .map(SalesOrder::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Long transactionCount = (long) cashierOrders.size();
            BigDecimal averageTicket = transactionCount > 0
                    ? totalSales.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            String cashierName = cashierOrders.get(0).getCashier().getFirstName() + " " +
                    cashierOrders.get(0).getCashier().getLastName();

            performances.add(AnalyticsDTO.CashierPerformance.builder()
                    .cashierId(cashierId)
                    .cashierName(cashierName)
                    .transactionCount(transactionCount)
                    .totalSales(totalSales)
                    .averageTicket(averageTicket)
                    .averageTransactionTime(BigDecimal.ZERO) // TODO: Calculate if we track transaction duration
                    .build());
        });

        return performances;
    }

    /**
     * Get top selling products
     */
    public AnalyticsDTO.TopProductsResponse getTopSellingProducts(LocalDate startDate, LocalDate endDate, Long storeId,
            String sortBy, Integer limit) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<SalesOrder> orders = salesOrderRepository.findByOrderDateBetweenAndStoreIdAndStatus(
                start, end, storeId, OrderStatus.COMPLETED);

        // Flatten all order items
        Map<Long, List<SalesOrderItem>> itemsByProduct = orders.stream()
                .flatMap(order -> order.getItems().stream())
                .collect(Collectors.groupingBy(item -> item.getProduct().getId()));

        List<AnalyticsDTO.ProductAnalytics> productAnalytics = new ArrayList<>();

        itemsByProduct.forEach((productId, items) -> {
            Long quantitySold = items.stream()
                    .mapToLong(SalesOrderItem::getQuantity)
                    .sum();

            BigDecimal revenue = items.stream()
                    .map(SalesOrderItem::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Long transactionCount = items.stream()
                    .map(item -> item.getId()) // Assuming each item is in a different transaction
                    .distinct()
                    .count();

            String productName = items.get(0).getProduct().getName();
            String sku = items.get(0).getProduct().getSku();

            productAnalytics.add(AnalyticsDTO.ProductAnalytics.builder()
                    .productId(productId)
                    .productName(productName)
                    .sku(sku)
                    .quantitySold(quantitySold)
                    .revenue(revenue)
                    .transactionCount(transactionCount)
                    .build());
        });

        // Sort and limit
        List<AnalyticsDTO.ProductAnalytics> sortedProducts = productAnalytics.stream()
                .sorted((p1, p2) -> {
                    if ("revenue".equals(sortBy)) {
                        return p2.getRevenue().compareTo(p1.getRevenue());
                    } else {
                        return p2.getQuantitySold().compareTo(p1.getQuantitySold());
                    }
                })
                .limit(limit != null ? limit : 10)
                .collect(Collectors.toList());

        return AnalyticsDTO.TopProductsResponse.builder()
                .products(sortedProducts)
                .sortBy(sortBy)
                .limit(limit)
                .build();
    }

    /**
     * Get low stock alerts
     */
    public List<AnalyticsDTO.LowStockAlert> getLowStockAlerts(Long storeId, Integer threshold) {
        List<Inventory> lowStockItems = inventoryRepository.findByStoreIdAndQuantityLessThan(
                storeId, threshold != null ? threshold : 10);

        return lowStockItems.stream()
                .map(inventory -> AnalyticsDTO.LowStockAlert.builder()
                        .productId(inventory.getProduct().getId())
                        .productName(inventory.getProduct().getName())
                        .sku(inventory.getProduct().getSku())
                        .currentStock(inventory.getCurrentQuantity())
                        .reorderLevel(10) // Default reorder level, can be enhanced later
                        .storeName(inventory.getStore().getName())
                        .build())
                .collect(Collectors.toList());
    }
}
