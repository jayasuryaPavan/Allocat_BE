package com.allocat.ai.service;

import com.allocat.inventory.entity.Inventory;
import com.allocat.inventory.entity.Product;
import com.allocat.inventory.repository.ProductRepository;
import com.allocat.inventory.service.InventoryService;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * LangChain4j tools for inventory operations.
 * These tools are used by the AI agent to interact with inventory data.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryTools {

    private final InventoryService inventoryService;
    private final ProductRepository productRepository;

    @Tool("Get current inventory levels for all products. Use when asked about stock levels, inventory status, or product availability.")
    public String getInventory(String query) {
        try {
            List<Inventory> inventory = inventoryService.getAvailableItems();
            if (inventory.isEmpty()) {
                return "No inventory items found.";
            }
            
            return formatInventoryList(inventory.subList(0, Math.min(20, inventory.size())), 
                                      "Current inventory (" + inventory.size() + " items)");
        } catch (Exception e) {
            log.error("Error getting inventory", e);
            return "Error fetching inventory: " + e.getMessage();
        }
    }

    @Tool("Get items that are low on stock. Use when asked about low stock items, items needing restocking, or products running out.")
    public String getLowStock(String query) {
        try {
            List<Inventory> items = inventoryService.getLowStockItems();
            if (items.isEmpty()) {
                return "No items are currently low on stock. All products have sufficient inventory.";
            }
            return formatLowStockList(items);
        } catch (Exception e) {
            log.error("Error getting low stock items", e);
            return "Error fetching low stock items: " + e.getMessage();
        }
    }

    @Tool("Get items that are completely out of stock (zero inventory). Use when asked about out of stock items or products with no inventory.")
    public String getOutOfStock(String query) {
        try {
            List<Inventory> items = inventoryService.getOutOfStockItems();
            if (items.isEmpty()) {
                return "Great news! No items are currently out of stock.";
            }
            return formatOutOfStockList(items);
        } catch (Exception e) {
            log.error("Error getting out of stock items", e);
            return "Error fetching out of stock items: " + e.getMessage();
        }
    }

    @Tool("Get inventory statistics and summary including total products, stock value, low stock count, etc. Use when asked about inventory overview, summary, statistics, or general inventory status.")
    public String getInventoryStats(String query) {
        try {
            List<Inventory> allItems = inventoryService.getAvailableItems();
            List<Inventory> lowStock = inventoryService.getLowStockItems();
            List<Inventory> outOfStock = inventoryService.getOutOfStockItems();
            
            int totalProducts = allItems.size();
            int totalAvailableQuantity = allItems.stream()
                    .mapToInt(inv -> inv.getAvailableQuantity() != null ? inv.getAvailableQuantity() : 0)
                    .sum();
            int totalReservedQuantity = allItems.stream()
                    .mapToInt(inv -> inv.getReservedQuantity() != null ? inv.getReservedQuantity() : 0)
                    .sum();
            
            double totalStockValue = allItems.stream()
                    .mapToDouble(inv -> inv.getTotalValue() != null ? inv.getTotalValue().doubleValue() : 0.0)
                    .sum();
            
            return String.format("""
                    Inventory Statistics:
                    - Total Products: %d
                    - Total Stock Value: %.2f
                    - Low Stock Items: %d
                    - Out of Stock Items: %d
                    - Total Available Quantity: %d
                    - Total Reserved Quantity: %d
                    """, 
                    totalProducts, totalStockValue, lowStock.size(), outOfStock.size(),
                    totalAvailableQuantity, totalReservedQuantity);
        } catch (Exception e) {
            log.error("Error getting inventory stats", e);
            return "Error fetching inventory stats: " + e.getMessage();
        }
    }

    @Tool("Search for products by name or code. Use when asked to find, search, or look up specific products. Input should be the search term (product name or code).")
    public String searchProducts(String searchTerm) {
        try {
            List<Product> products = productRepository.searchProducts(searchTerm);
            if (products.isEmpty()) {
                return String.format("No products found matching '%s'", searchTerm);
            }
            return formatProductList(products, String.format("Products found (%d total)", products.size()));
        } catch (Exception e) {
            log.error("Error searching products", e);
            return "Error searching products: " + e.getMessage();
        }
    }

    @Tool("Get detailed information about a specific product by its ID number. Use when asked about a specific product by ID. Input should be the product ID number as a string.")
    public String getProductById(String productId) {
        try {
            Long id = Long.parseLong(productId.trim());
            return productRepository.findById(id)
                    .map(product -> formatProductDetails(product))
                    .orElse("Product not found with ID: " + productId);
        } catch (NumberFormatException e) {
            return "Invalid product ID: " + productId + ". Please provide a numeric ID.";
        } catch (Exception e) {
            log.error("Error getting product by ID", e);
            return "Error fetching product: " + e.getMessage();
        }
    }

    @Tool("Get inventory discrepancies - differences between expected and actual stock levels. Use when asked about discrepancies, stock differences, or inventory issues.")
    public String getStockDiscrepancies(String query) {
        try {
            var discrepancies = inventoryService.getDiscrepancies();
            if (discrepancies.isEmpty()) {
                return "No inventory discrepancies found. All stock levels match expected values.";
            }
            return formatReceivedStockDiscrepancies(discrepancies);
        } catch (Exception e) {
            log.error("Error getting discrepancies", e);
            return "Error fetching discrepancies: " + e.getMessage();
        }
    }

    private String formatInventoryList(List<Inventory> inventory, String header) {
        StringBuilder sb = new StringBuilder(header).append(":\n");
        for (Inventory inv : inventory) {
            sb.append(String.format("Product ID: %d, Product: %s, Stock: %d, Reserved: %d\n",
                    inv.getProduct().getId(),
                    inv.getProduct().getName(),
                    inv.getAvailableQuantity() != null ? inv.getAvailableQuantity() : 0,
                    inv.getReservedQuantity() != null ? inv.getReservedQuantity() : 0));
        }
        return sb.toString().trim();
    }

    private String formatLowStockList(List<Inventory> items) {
        StringBuilder sb = new StringBuilder(String.format("Low stock items (%d items):\n", items.size()));
        for (Inventory inv : items) {
            sb.append(String.format("Product: %s (ID: %d), Current Stock: %d\n",
                    inv.getProduct().getName(),
                    inv.getProduct().getId(),
                    inv.getAvailableQuantity() != null ? inv.getAvailableQuantity() : 0));
        }
        return sb.toString().trim();
    }

    private String formatOutOfStockList(List<Inventory> items) {
        StringBuilder sb = new StringBuilder(String.format("Out of stock items (%d items):\n", items.size()));
        for (Inventory inv : items) {
            sb.append(String.format("Product: %s (ID: %d)\n",
                    inv.getProduct().getName(),
                    inv.getProduct().getId()));
        }
        return sb.toString().trim();
    }

    private String formatProductList(List<Product> products, String header) {
        StringBuilder sb = new StringBuilder(header).append(":\n");
        for (Product p : products.subList(0, Math.min(10, products.size()))) {
            sb.append(String.format("Product: %s (Code: %s, ID: %d), Price: %s, Category: %s\n",
                    p.getName() != null ? p.getName() : "N/A",
                    p.getProductCode() != null ? p.getProductCode() : "N/A",
                    p.getId(),
                    p.getUnitPrice() != null ? p.getUnitPrice().toString() : "N/A",
                    p.getCategory() != null ? p.getCategory() : "N/A"));
        }
        return sb.toString().trim();
    }

    private String formatProductDetails(Product product) {
        return String.format("""
                Product Details:
                - Name: %s
                - Code: %s
                - ID: %d
                - Price: %s
                - Category: %s
                - Description: %s
                """,
                product.getName() != null ? product.getName() : "N/A",
                product.getProductCode() != null ? product.getProductCode() : "N/A",
                product.getId(),
                product.getUnitPrice() != null ? product.getUnitPrice().toString() : "N/A",
                product.getCategory() != null ? product.getCategory() : "N/A",
                product.getDescription() != null ? product.getDescription() : "N/A");
    }

    private String formatReceivedStockDiscrepancies(List<com.allocat.inventory.entity.ReceivedStock> discrepancies) {
        StringBuilder sb = new StringBuilder(String.format("Stock discrepancies (%d items):\n", discrepancies.size()));
        for (var rs : discrepancies) {
            sb.append(String.format("Product: %s, Expected: %d, Verified: %d, Difference: %d\n",
                    rs.getProduct() != null ? rs.getProduct().getName() : "N/A",
                    rs.getExpectedQuantity() != null ? rs.getExpectedQuantity() : 0,
                    rs.getVerifiedQuantity() != null ? rs.getVerifiedQuantity() : 0,
                    (rs.getExpectedQuantity() != null ? rs.getExpectedQuantity() : 0) - 
                    (rs.getVerifiedQuantity() != null ? rs.getVerifiedQuantity() : 0)));
        }
        return sb.toString().trim();
    }
}

