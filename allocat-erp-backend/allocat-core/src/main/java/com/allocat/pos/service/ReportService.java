package com.allocat.pos.service;

import com.allocat.auth.entity.Store;
import com.allocat.auth.entity.User;
import com.allocat.auth.repository.StoreRepository;
import com.allocat.auth.repository.UserRepository;
import com.allocat.pos.dto.ReportDTO;
import com.allocat.pos.entity.Payment;
import com.allocat.pos.entity.SalesOrder;
import com.allocat.pos.entity.SalesOrderItem;
import com.allocat.pos.entity.Shift;
import com.allocat.pos.enums.OrderStatus;
import com.allocat.pos.enums.PaymentType;
import com.allocat.pos.repository.SalesOrderRepository;
import com.allocat.pos.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating Shift and Day Reports
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportService {

    private final ShiftRepository shiftRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    // ============== SHIFT REPORT ==============

    /**
     * Get detailed shift report for a specific shift
     */
    public ReportDTO.ShiftReport getShiftReport(Long shiftId) {
        log.info("Generating shift report for shiftId: {}", shiftId);

        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found: " + shiftId));

        User employee = userRepository.findById(shift.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + shift.getUserId()));

        Store store = storeRepository.findById(shift.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found: " + shift.getStoreId()));

        // Get shift time boundaries
        LocalDateTime startTime = shift.getStartedAt();
        LocalDateTime endTime = shift.getEndedAt() != null ? shift.getEndedAt() : LocalDateTime.now();

        // Get orders for this shift
        List<SalesOrder> orders = salesOrderRepository.findByCashierIdAndDateRange(
                shift.getUserId(), startTime, endTime, OrderStatus.COMPLETED);

        // Calculate sales metrics
        BigDecimal totalSales = calculateTotalSales(orders);
        BigDecimal taxCollected = calculateTaxCollected(orders);
        BigDecimal discountsGiven = calculateDiscounts(orders);
        BigDecimal averageTicket = orders.isEmpty() ? BigDecimal.ZERO :
                totalSales.divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP);

        // Calculate shift duration
        Long durationMinutes = Duration.between(startTime, endTime).toMinutes();

        // Get payment breakdown
        List<ReportDTO.PaymentBreakdown> paymentBreakdown = getPaymentBreakdown(orders, totalSales);

        // Get top products
        List<ReportDTO.ProductSold> topProducts = getTopProducts(orders, 10);

        // Get order summaries
        List<ReportDTO.OrderSummary> orderSummaries = orders.stream()
                .map(this::mapToOrderSummary)
                .sorted(Comparator.comparing(ReportDTO.OrderSummary::getOrderDate).reversed())
                .collect(Collectors.toList());

        return ReportDTO.ShiftReport.builder()
                .shiftId(shiftId)
                .employeeName(employee.getFirstName() + " " + employee.getLastName())
                .employeeId(employee.getId())
                .storeName(store.getName())
                .storeId(store.getId())
                .shiftDate(shift.getShiftDate())
                .startTime(startTime)
                .endTime(shift.getEndedAt())
                .status(shift.getStatus().name())
                .durationMinutes(durationMinutes)
                .startingCash(shift.getStartingCashAmount())
                .endingCash(shift.getEndingCashAmount())
                .expectedCash(shift.getExpectedCashAmount())
                .cashDifference(shift.getCashDifference())
                .totalSales(totalSales)
                .totalOrders((long) orders.size())
                .averageTicket(averageTicket)
                .taxCollected(taxCollected)
                .discountsGiven(discountsGiven)
                .paymentBreakdown(paymentBreakdown)
                .topProducts(topProducts)
                .orders(orderSummaries)
                .notes(shift.getNotes())
                .build();
    }

    // ============== PROFIT REPORT ==============

    /**
     * Get comprehensive profit report for a store within a date range
     * Calculates profit as: Revenue (after discounts) - Cost of Goods Sold
     */
    public ReportDTO.ProfitReport getProfitReport(Long storeId, LocalDate startDate, LocalDate endDate) {
        log.info("Generating profit report for storeId: {} from {} to {}", storeId, startDate, endDate);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found: " + storeId));

        // Get all completed orders for the date range
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        List<SalesOrder> orders = salesOrderRepository.findByOrderDateBetweenAndStoreIdAndStatus(
                start, end, storeId, OrderStatus.COMPLETED);

        // Calculate overall metrics
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalDiscounts = BigDecimal.ZERO;
        long totalItemsSold = 0;

        // Product-level profit tracking
        Map<Long, ProductProfitAccumulator> productProfitMap = new HashMap<>();
        
        // Category-level profit tracking
        Map<String, CategoryProfitAccumulator> categoryProfitMap = new HashMap<>();
        
        // Daily profit tracking
        Map<LocalDate, DailyProfitAccumulator> dailyProfitMap = new HashMap<>();

        for (SalesOrder order : orders) {
            LocalDate orderDate = order.getOrderDate().toLocalDate();
            BigDecimal orderDiscount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
            totalDiscounts = totalDiscounts.add(orderDiscount);

            // Get daily accumulator
            DailyProfitAccumulator dailyAcc = dailyProfitMap.computeIfAbsent(orderDate, 
                    d -> new DailyProfitAccumulator());
            dailyAcc.orderCount++;

            if (order.getItems() != null) {
                for (SalesOrderItem item : order.getItems()) {
                    BigDecimal itemRevenue = item.getTotal() != null ? item.getTotal() : BigDecimal.ZERO;
                    BigDecimal itemCost = (item.getCostPrice() != null ? item.getCostPrice() : BigDecimal.ZERO)
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    BigDecimal itemDiscount = item.getDiscount() != null ? item.getDiscount() : BigDecimal.ZERO;
                    
                    totalRevenue = totalRevenue.add(itemRevenue);
                    totalCost = totalCost.add(itemCost);
                    totalItemsSold += item.getQuantity();

                    // Update daily accumulator
                    dailyAcc.revenue = dailyAcc.revenue.add(itemRevenue);
                    dailyAcc.cost = dailyAcc.cost.add(itemCost);
                    dailyAcc.discount = dailyAcc.discount.add(itemDiscount);
                    dailyAcc.itemsSold += item.getQuantity();

                    // Update product accumulator
                    Long productId = item.getProduct().getId();
                    ProductProfitAccumulator prodAcc = productProfitMap.computeIfAbsent(productId,
                            id -> new ProductProfitAccumulator(item.getProduct()));
                    prodAcc.quantitySold += item.getQuantity();
                    prodAcc.totalRevenue = prodAcc.totalRevenue.add(itemRevenue);
                    prodAcc.totalCost = prodAcc.totalCost.add(itemCost);
                    prodAcc.totalDiscount = prodAcc.totalDiscount.add(itemDiscount);
                    prodAcc.sellingPriceSum = prodAcc.sellingPriceSum.add(item.getUnitPrice());
                    prodAcc.costPriceSum = prodAcc.costPriceSum.add(
                            item.getCostPrice() != null ? item.getCostPrice() : BigDecimal.ZERO);
                    prodAcc.priceCount++;

                    // Update category accumulator
                    String category = item.getProduct().getCategory() != null ? 
                            item.getProduct().getCategory() : "Uncategorized";
                    CategoryProfitAccumulator catAcc = categoryProfitMap.computeIfAbsent(category,
                            c -> new CategoryProfitAccumulator());
                    catAcc.productIds.add(productId);
                    catAcc.quantitySold += item.getQuantity();
                    catAcc.totalRevenue = catAcc.totalRevenue.add(itemRevenue);
                    catAcc.totalCost = catAcc.totalCost.add(itemCost);
                    catAcc.totalDiscount = catAcc.totalDiscount.add(itemDiscount);
                }
            }
        }

        // Calculate overall profit metrics
        BigDecimal grossProfit = totalRevenue.subtract(totalCost);
        BigDecimal netProfit = grossProfit; // Discounts are already factored into revenue
        BigDecimal profitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? netProfit.multiply(BigDecimal.valueOf(100))
                        .divide(totalRevenue, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Build product profit list
        List<ReportDTO.ProductProfit> productProfits = buildProductProfits(productProfitMap, netProfit);
        
        // Build category profit list
        List<ReportDTO.CategoryProfit> categoryProfits = buildCategoryProfits(categoryProfitMap, netProfit);
        
        // Build daily profit list
        List<ReportDTO.DailyProfit> dailyProfits = buildDailyProfits(dailyProfitMap);

        // Get top and least profitable products
        List<ReportDTO.ProductProfit> topProfitable = productProfits.stream()
                .sorted((a, b) -> b.getNetProfit().compareTo(a.getNetProfit()))
                .limit(10)
                .collect(Collectors.toList());

        List<ReportDTO.ProductProfit> leastProfitable = productProfits.stream()
                .sorted(Comparator.comparing(ReportDTO.ProductProfit::getNetProfit))
                .limit(10)
                .collect(Collectors.toList());

        return ReportDTO.ProfitReport.builder()
                .startDate(startDate)
                .endDate(endDate)
                .storeName(store.getName())
                .storeId(storeId)
                .totalRevenue(totalRevenue)
                .totalCost(totalCost)
                .grossProfit(grossProfit)
                .totalDiscounts(totalDiscounts)
                .netProfit(netProfit)
                .profitMargin(profitMargin)
                .totalOrders((long) orders.size())
                .totalItemsSold(totalItemsSold)
                .productProfits(productProfits)
                .categoryProfits(categoryProfits)
                .dailyProfits(dailyProfits)
                .topProfitableProducts(topProfitable)
                .leastProfitableProducts(leastProfitable)
                .build();
    }

    // Helper classes for profit accumulation
    private static class ProductProfitAccumulator {
        com.allocat.inventory.entity.Product product;
        long quantitySold = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal sellingPriceSum = BigDecimal.ZERO;
        BigDecimal costPriceSum = BigDecimal.ZERO;
        int priceCount = 0;

        ProductProfitAccumulator(com.allocat.inventory.entity.Product product) {
            this.product = product;
        }
    }

    private static class CategoryProfitAccumulator {
        Set<Long> productIds = new HashSet<>();
        long quantitySold = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
    }

    private static class DailyProfitAccumulator {
        long orderCount = 0;
        long itemsSold = 0;
        BigDecimal revenue = BigDecimal.ZERO;
        BigDecimal cost = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;
    }

    private List<ReportDTO.ProductProfit> buildProductProfits(
            Map<Long, ProductProfitAccumulator> productProfitMap, BigDecimal totalNetProfit) {
        
        List<ReportDTO.ProductProfit> profits = new ArrayList<>();
        
        for (Map.Entry<Long, ProductProfitAccumulator> entry : productProfitMap.entrySet()) {
            ProductProfitAccumulator acc = entry.getValue();
            
            BigDecimal grossProfit = acc.totalRevenue.subtract(acc.totalCost);
            BigDecimal netProfit = grossProfit; // Discounts already in revenue
            BigDecimal profitMargin = acc.totalRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? netProfit.multiply(BigDecimal.valueOf(100))
                            .divide(acc.totalRevenue, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BigDecimal profitPerUnit = acc.quantitySold > 0
                    ? netProfit.divide(BigDecimal.valueOf(acc.quantitySold), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BigDecimal avgSellingPrice = acc.priceCount > 0
                    ? acc.sellingPriceSum.divide(BigDecimal.valueOf(acc.priceCount), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BigDecimal avgCostPrice = acc.priceCount > 0
                    ? acc.costPriceSum.divide(BigDecimal.valueOf(acc.priceCount), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            profits.add(ReportDTO.ProductProfit.builder()
                    .productId(entry.getKey())
                    .productName(acc.product.getName())
                    .sku(acc.product.getSku())
                    .category(acc.product.getCategory())
                    .quantitySold(acc.quantitySold)
                    .avgSellingPrice(avgSellingPrice)
                    .avgCostPrice(avgCostPrice)
                    .totalRevenue(acc.totalRevenue)
                    .totalCost(acc.totalCost)
                    .totalDiscount(acc.totalDiscount)
                    .grossProfit(grossProfit)
                    .netProfit(netProfit)
                    .profitMargin(profitMargin)
                    .profitPerUnit(profitPerUnit)
                    .build());
        }

        // Sort by net profit descending and assign ranks
        profits.sort((a, b) -> b.getNetProfit().compareTo(a.getNetProfit()));
        for (int i = 0; i < profits.size(); i++) {
            profits.get(i).setRank(i + 1);
        }

        return profits;
    }

    private List<ReportDTO.CategoryProfit> buildCategoryProfits(
            Map<String, CategoryProfitAccumulator> categoryProfitMap, BigDecimal totalNetProfit) {
        
        List<ReportDTO.CategoryProfit> profits = new ArrayList<>();
        
        for (Map.Entry<String, CategoryProfitAccumulator> entry : categoryProfitMap.entrySet()) {
            CategoryProfitAccumulator acc = entry.getValue();
            
            BigDecimal grossProfit = acc.totalRevenue.subtract(acc.totalCost);
            BigDecimal netProfit = grossProfit;
            BigDecimal profitMargin = acc.totalRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? netProfit.multiply(BigDecimal.valueOf(100))
                            .divide(acc.totalRevenue, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BigDecimal percentageOfTotal = totalNetProfit.compareTo(BigDecimal.ZERO) > 0
                    ? netProfit.multiply(BigDecimal.valueOf(100))
                            .divide(totalNetProfit, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            profits.add(ReportDTO.CategoryProfit.builder()
                    .category(entry.getKey())
                    .productCount((long) acc.productIds.size())
                    .quantitySold(acc.quantitySold)
                    .totalRevenue(acc.totalRevenue)
                    .totalCost(acc.totalCost)
                    .totalDiscount(acc.totalDiscount)
                    .grossProfit(grossProfit)
                    .netProfit(netProfit)
                    .profitMargin(profitMargin)
                    .percentageOfTotalProfit(percentageOfTotal)
                    .build());
        }

        // Sort by net profit descending
        profits.sort((a, b) -> b.getNetProfit().compareTo(a.getNetProfit()));
        return profits;
    }

    private List<ReportDTO.DailyProfit> buildDailyProfits(Map<LocalDate, DailyProfitAccumulator> dailyProfitMap) {
        List<ReportDTO.DailyProfit> profits = new ArrayList<>();
        
        for (Map.Entry<LocalDate, DailyProfitAccumulator> entry : dailyProfitMap.entrySet()) {
            DailyProfitAccumulator acc = entry.getValue();
            
            BigDecimal grossProfit = acc.revenue.subtract(acc.cost);
            BigDecimal netProfit = grossProfit;
            BigDecimal profitMargin = acc.revenue.compareTo(BigDecimal.ZERO) > 0
                    ? netProfit.multiply(BigDecimal.valueOf(100))
                            .divide(acc.revenue, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            profits.add(ReportDTO.DailyProfit.builder()
                    .date(entry.getKey())
                    .orderCount(acc.orderCount)
                    .itemsSold(acc.itemsSold)
                    .revenue(acc.revenue)
                    .cost(acc.cost)
                    .discount(acc.discount)
                    .grossProfit(grossProfit)
                    .netProfit(netProfit)
                    .profitMargin(profitMargin)
                    .build());
        }

        // Sort by date ascending
        profits.sort(Comparator.comparing(ReportDTO.DailyProfit::getDate));
        return profits;
    }

    // ============== DAY REPORT ==============

    /**
     * Get comprehensive day report for a store on a specific date
     */
    public ReportDTO.DayReport getDayReport(Long storeId, LocalDate date) {
        log.info("Generating day report for storeId: {} on date: {}", storeId, date);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found: " + storeId));

        // Get all shifts for the day
        List<Shift> shifts = shiftRepository.findByStoreIdAndShiftDate(storeId, date);

        // Get all orders for the day
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        List<SalesOrder> orders = salesOrderRepository.findByOrderDateBetweenAndStoreIdAndStatus(
                dayStart, dayEnd, storeId, OrderStatus.COMPLETED);

        // Calculate sales metrics
        BigDecimal totalSales = calculateTotalSales(orders);
        BigDecimal taxCollected = calculateTaxCollected(orders);
        BigDecimal discountsGiven = calculateDiscounts(orders);
        BigDecimal averageTicket = orders.isEmpty() ? BigDecimal.ZERO :
                totalSales.divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP);

        // Calculate cash summary across all shifts
        BigDecimal totalStartingCash = shifts.stream()
                .map(s -> s.getStartingCashAmount() != null ? s.getStartingCashAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalEndingCash = shifts.stream()
                .filter(s -> s.getEndingCashAmount() != null)
                .map(Shift::getEndingCashAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCashDifference = shifts.stream()
                .filter(s -> s.getCashDifference() != null)
                .map(Shift::getCashDifference)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Count shift statuses
        int activeShifts = (int) shifts.stream()
                .filter(s -> s.getStatus() == Shift.ShiftStatus.ACTIVE).count();
        int completedShifts = (int) shifts.stream()
                .filter(s -> s.getStatus() == Shift.ShiftStatus.COMPLETED).count();

        // Get payment breakdown
        List<ReportDTO.PaymentBreakdown> paymentBreakdown = getPaymentBreakdown(orders, totalSales);

        // Get hourly breakdown
        List<ReportDTO.HourlySales> hourlyBreakdown = getHourlyBreakdown(orders);

        // Get top products
        List<ReportDTO.ProductSold> topProducts = getTopProducts(orders, 10);

        // Get employee performance
        List<ReportDTO.EmployeePerformance> employeePerformance = getEmployeePerformance(orders, shifts);

        // Get shift summaries
        List<ReportDTO.ShiftSummary> shiftSummaries = getShiftSummaries(shifts, orders);

        return ReportDTO.DayReport.builder()
                .date(date)
                .storeName(store.getName())
                .storeId(storeId)
                .totalShifts(shifts.size())
                .activeShifts(activeShifts)
                .completedShifts(completedShifts)
                .totalSales(totalSales)
                .totalOrders((long) orders.size())
                .averageTicket(averageTicket)
                .taxCollected(taxCollected)
                .discountsGiven(discountsGiven)
                .totalStartingCash(totalStartingCash)
                .totalEndingCash(totalEndingCash)
                .totalCashDifference(totalCashDifference)
                .paymentBreakdown(paymentBreakdown)
                .hourlyBreakdown(hourlyBreakdown)
                .topProducts(topProducts)
                .employeePerformance(employeePerformance)
                .shifts(shiftSummaries)
                .build();
    }

    // ============== HELPER METHODS ==============

    private BigDecimal calculateTotalSales(List<SalesOrder> orders) {
        return orders.stream()
                .map(SalesOrder::getTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTaxCollected(List<SalesOrder> orders) {
        return orders.stream()
                .map(SalesOrder::getTaxAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscounts(List<SalesOrder> orders) {
        return orders.stream()
                .map(SalesOrder::getDiscountAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<ReportDTO.PaymentBreakdown> getPaymentBreakdown(List<SalesOrder> orders, BigDecimal totalSales) {
        Map<PaymentType, BigDecimal> paymentTotals = new HashMap<>();
        Map<PaymentType, Long> paymentCounts = new HashMap<>();

        for (SalesOrder order : orders) {
            if (order.getPayments() != null) {
                for (Payment payment : order.getPayments()) {
                    PaymentType type = payment.getPaymentType();
                    BigDecimal amount = payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO;
                    
                    paymentTotals.merge(type, amount, BigDecimal::add);
                    paymentCounts.merge(type, 1L, Long::sum);
                }
            }
        }

        List<ReportDTO.PaymentBreakdown> breakdown = new ArrayList<>();
        for (PaymentType type : paymentTotals.keySet()) {
            BigDecimal amount = paymentTotals.get(type);
            BigDecimal percentage = totalSales.compareTo(BigDecimal.ZERO) > 0
                    ? amount.multiply(BigDecimal.valueOf(100))
                            .divide(totalSales, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            breakdown.add(ReportDTO.PaymentBreakdown.builder()
                    .paymentType(type.name())
                    .amount(amount)
                    .transactionCount(paymentCounts.get(type))
                    .percentage(percentage)
                    .build());
        }

        // Sort by amount descending
        breakdown.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));
        return breakdown;
    }

    private List<ReportDTO.ProductSold> getTopProducts(List<SalesOrder> orders, int limit) {
        Map<Long, ReportDTO.ProductSold> productMap = new HashMap<>();

        for (SalesOrder order : orders) {
            if (order.getItems() != null) {
                for (SalesOrderItem item : order.getItems()) {
                    Long productId = item.getProduct().getId();
                    
                    ReportDTO.ProductSold existing = productMap.get(productId);
                    if (existing == null) {
                        existing = ReportDTO.ProductSold.builder()
                                .productId(productId)
                                .productName(item.getProduct().getName())
                                .sku(item.getProduct().getSku())
                                .unitPrice(item.getUnitPrice())
                                .quantitySold(0L)
                                .revenue(BigDecimal.ZERO)
                                .build();
                        productMap.put(productId, existing);
                    }
                    
                    existing.setQuantitySold(existing.getQuantitySold() + item.getQuantity());
                    existing.setRevenue(existing.getRevenue().add(
                            item.getTotal() != null ? item.getTotal() : BigDecimal.ZERO));
                }
            }
        }

        return productMap.values().stream()
                .sorted((a, b) -> b.getRevenue().compareTo(a.getRevenue()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<ReportDTO.HourlySales> getHourlyBreakdown(List<SalesOrder> orders) {
        Map<Integer, List<SalesOrder>> ordersByHour = orders.stream()
                .collect(Collectors.groupingBy(order -> order.getOrderDate().getHour()));

        List<ReportDTO.HourlySales> hourlyBreakdown = new ArrayList<>();

        for (int hour = 0; hour < 24; hour++) {
            List<SalesOrder> hourOrders = ordersByHour.getOrDefault(hour, Collections.emptyList());
            
            if (!hourOrders.isEmpty()) {
                BigDecimal sales = calculateTotalSales(hourOrders);
                BigDecimal avgTicket = sales.divide(
                        BigDecimal.valueOf(hourOrders.size()), 2, RoundingMode.HALF_UP);

                String hourLabel = String.format("%02d:00 - %02d:00",
                        hour, (hour + 1) % 24);

                hourlyBreakdown.add(ReportDTO.HourlySales.builder()
                        .hour(hour)
                        .hourLabel(hourLabel)
                        .sales(sales)
                        .orders((long) hourOrders.size())
                        .averageTicket(avgTicket)
                        .build());
            }
        }

        return hourlyBreakdown;
    }

    private List<ReportDTO.EmployeePerformance> getEmployeePerformance(
            List<SalesOrder> orders, List<Shift> shifts) {
        
        // Group orders by cashier
        Map<Long, List<SalesOrder>> ordersByCashier = orders.stream()
                .filter(o -> o.getCashier() != null)
                .collect(Collectors.groupingBy(o -> o.getCashier().getId()));

        // Group shifts by user
        Map<Long, List<Shift>> shiftsByUser = shifts.stream()
                .collect(Collectors.groupingBy(Shift::getUserId));

        List<ReportDTO.EmployeePerformance> performances = new ArrayList<>();

        Set<Long> allEmployeeIds = new HashSet<>();
        allEmployeeIds.addAll(ordersByCashier.keySet());
        allEmployeeIds.addAll(shiftsByUser.keySet());

        for (Long employeeId : allEmployeeIds) {
            List<SalesOrder> empOrders = ordersByCashier.getOrDefault(employeeId, Collections.emptyList());
            List<Shift> empShifts = shiftsByUser.getOrDefault(employeeId, Collections.emptyList());

            BigDecimal totalSales = calculateTotalSales(empOrders);
            long totalOrders = empOrders.size();
            BigDecimal avgTicket = totalOrders > 0
                    ? totalSales.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            // Calculate total minutes worked
            long totalMinutes = empShifts.stream()
                    .mapToLong(s -> {
                        LocalDateTime start = s.getStartedAt();
                        LocalDateTime end = s.getEndedAt() != null ? s.getEndedAt() : LocalDateTime.now();
                        return Duration.between(start, end).toMinutes();
                    })
                    .sum();

            // Calculate sales per hour
            BigDecimal salesPerHour = totalMinutes > 0
                    ? totalSales.multiply(BigDecimal.valueOf(60))
                            .divide(BigDecimal.valueOf(totalMinutes), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            // Get employee name
            String employeeName = empOrders.isEmpty()
                    ? userRepository.findById(employeeId)
                            .map(u -> u.getFirstName() + " " + u.getLastName())
                            .orElse("Unknown")
                    : empOrders.get(0).getCashier().getFirstName() + " " +
                      empOrders.get(0).getCashier().getLastName();

            performances.add(ReportDTO.EmployeePerformance.builder()
                    .employeeId(employeeId)
                    .employeeName(employeeName)
                    .totalSales(totalSales)
                    .totalOrders(totalOrders)
                    .averageTicket(avgTicket)
                    .shiftCount((long) empShifts.size())
                    .totalMinutesWorked(totalMinutes)
                    .salesPerHour(salesPerHour)
                    .build());
        }

        // Sort by total sales descending and assign ranks
        performances.sort((a, b) -> b.getTotalSales().compareTo(a.getTotalSales()));
        for (int i = 0; i < performances.size(); i++) {
            performances.get(i).setRank(i + 1);
        }

        return performances;
    }

    private List<ReportDTO.ShiftSummary> getShiftSummaries(List<Shift> shifts, List<SalesOrder> allOrders) {
        List<ReportDTO.ShiftSummary> summaries = new ArrayList<>();

        for (Shift shift : shifts) {
            User employee = userRepository.findById(shift.getUserId()).orElse(null);
            String employeeName = employee != null
                    ? employee.getFirstName() + " " + employee.getLastName()
                    : "Unknown";

            LocalDateTime startTime = shift.getStartedAt();
            LocalDateTime endTime = shift.getEndedAt() != null ? shift.getEndedAt() : LocalDateTime.now();

            // Filter orders for this shift
            List<SalesOrder> shiftOrders = allOrders.stream()
                    .filter(o -> o.getCashier() != null &&
                            o.getCashier().getId().equals(shift.getUserId()) &&
                            !o.getOrderDate().isBefore(startTime) &&
                            !o.getOrderDate().isAfter(endTime))
                    .collect(Collectors.toList());

            BigDecimal shiftSales = calculateTotalSales(shiftOrders);
            long durationMinutes = Duration.between(startTime, endTime).toMinutes();

            summaries.add(ReportDTO.ShiftSummary.builder()
                    .shiftId(shift.getId())
                    .employeeName(employeeName)
                    .employeeId(shift.getUserId())
                    .startTime(startTime)
                    .endTime(shift.getEndedAt())
                    .status(shift.getStatus().name())
                    .durationMinutes(durationMinutes)
                    .totalSales(shiftSales)
                    .totalOrders((long) shiftOrders.size())
                    .startingCash(shift.getStartingCashAmount())
                    .endingCash(shift.getEndingCashAmount())
                    .cashDifference(shift.getCashDifference())
                    .build());
        }

        // Sort by start time
        summaries.sort(Comparator.comparing(ReportDTO.ShiftSummary::getStartTime));
        return summaries;
    }

    private ReportDTO.OrderSummary mapToOrderSummary(SalesOrder order) {
        // Get primary payment type
        String paymentType = order.getPayments() != null && !order.getPayments().isEmpty()
                ? order.getPayments().get(0).getPaymentType().name()
                : "N/A";

        int itemCount = order.getItems() != null ? order.getItems().size() : 0;

        return ReportDTO.OrderSummary.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .orderDate(order.getOrderDate())
                .subtotal(order.getSubtotal())
                .tax(order.getTaxAmount())
                .discount(order.getDiscountAmount())
                .total(order.getTotal())
                .paymentType(paymentType)
                .status(order.getStatus().name())
                .itemCount(itemCount)
                .build();
    }
}

