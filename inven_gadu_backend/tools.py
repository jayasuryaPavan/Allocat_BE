from langchain.tools import Tool
from typing import Optional
import requests
from config import settings
import json

class SpringBootTools:
    """Tools that interact with Spring Boot backend API"""
    
    def __init__(self, auth_token: Optional[str] = None):
        self.backend_url = settings.backend_url
        self.timeout = settings.backend_timeout
        self.auth_token = auth_token
        self.headers = {
            "Content-Type": "application/json",
            "Accept": "application/json"
        }
        if auth_token:
            self.headers["Authorization"] = f"Bearer {auth_token}"
    
    def _make_request(self, method: str, endpoint: str, **kwargs) -> dict:
        """Make HTTP request to Spring Boot backend"""
        url = f"{self.backend_url}{endpoint}"
        try:
            response = requests.request(
                method=method,
                url=url,
                headers=self.headers,
                timeout=self.timeout,
                **kwargs
            )
            response.raise_for_status()
            return response.json()
        except requests.exceptions.Timeout:
            return {"error": "Request timeout. Backend is not responding."}
        except requests.exceptions.ConnectionError:
            return {"error": "Cannot connect to backend. Ensure Spring Boot is running."}
        except requests.exceptions.HTTPError as e:
            return {"error": f"HTTP error: {e.response.status_code} - {e.response.text}"}
        except Exception as e:
            return {"error": f"Unexpected error: {str(e)}"}
    
    def get_inventory(self, query: str) -> str:
        """
        Get current inventory levels for all products.
        Use this when asked about stock levels, inventory status, or product availability.
        """
        result = self._make_request("GET", "/inventory/current")
        if "error" in result:
            return f"Error fetching inventory: {result['error']}"
        
        if isinstance(result, dict) and "data" in result:
            inventory = result["data"]
            if isinstance(inventory, list):
                formatted = "\n".join([
                    f"Product ID: {item.get('productId', 'N/A')}, "
                    f"Product: {item.get('productName', 'N/A')}, "
                    f"Stock: {item.get('availableQuantity', 0)}, "
                    f"Reserved: {item.get('reservedQuantity', 0)}"
                    for item in inventory[:20]
                ])
                return f"Current inventory ({len(inventory)} items):\n{formatted}"
        return json.dumps(result, indent=2)
    
    def get_low_stock(self, query: str) -> str:
        """
        Get items that are low on stock.
        Use this when asked about low stock items, items needing restocking, or products running out.
        """
        result = self._make_request("GET", "/inventory/low-stock")
        if "error" in result:
            return f"Error fetching low stock items: {result['error']}"
        
        if isinstance(result, dict) and "data" in result:
            items = result["data"]
            if isinstance(items, list):
                if len(items) == 0:
                    return "No items are currently low on stock. All products have sufficient inventory."
                formatted = "\n".join([
                    f"Product: {item.get('productName', 'N/A')} (ID: {item.get('productId', 'N/A')}), "
                    f"Current Stock: {item.get('availableQuantity', 0)}, "
                    f"Minimum Required: {item.get('minimumStockLevel', 'N/A')}"
                    for item in items
                ])
                return f"Low stock items ({len(items)} items):\n{formatted}"
        return json.dumps(result, indent=2)
    
    def get_out_of_stock(self, query: str) -> str:
        """
        Get items that are out of stock.
        Use this when asked about out of stock items or products with zero inventory.
        """
        result = self._make_request("GET", "/inventory/out-of-stock")
        if "error" in result:
            return f"Error fetching out of stock items: {result['error']}"
        
        if isinstance(result, dict) and "data" in result:
            items = result["data"]
            if isinstance(items, list):
                if len(items) == 0:
                    return "Great news! No items are currently out of stock."
                formatted = "\n".join([
                    f"Product: {item.get('productName', 'N/A')} (ID: {item.get('productId', 'N/A')})"
                    for item in items
                ])
                return f"Out of stock items ({len(items)} items):\n{formatted}"
        return json.dumps(result, indent=2)
    
    def get_inventory_stats(self, query: str) -> str:
        """
        Get inventory statistics and summary.
        Use this when asked about inventory overview, summary, statistics, or general inventory status.
        """
        result = self._make_request("GET", "/inventory/stats")
        if "error" in result:
            return f"Error fetching inventory stats: {result['error']}"
        
        if isinstance(result, dict) and "data" in result:
            stats = result["data"]
            formatted = f"""
Inventory Statistics:
- Total Products: {stats.get('totalProducts', 'N/A')}
- Total Stock Value: {stats.get('totalStockValue', 'N/A')}
- Low Stock Items: {stats.get('lowStockCount', 'N/A')}
- Out of Stock Items: {stats.get('outOfStockCount', 'N/A')}
- Total Available Quantity: {stats.get('totalAvailableQuantity', 'N/A')}
- Total Reserved Quantity: {stats.get('totalReservedQuantity', 'N/A')}
"""
            return formatted.strip()
        return json.dumps(result, indent=2)
    
    def search_products(self, query: str) -> str:
        """
        Search for products by name or code.
        Use this when asked to find, search, or look up specific products.
        Input should be the search term.
        """
        result = self._make_request(
            "GET",
            "/products/search",
            params={"searchTerm": query}
        )
        if "error" in result:
            return f"Error searching products: {result['error']}"
        
        if isinstance(result, dict) and "data" in result:
            products = result["data"]
            if isinstance(products, list):
                if len(products) == 0:
                    return f"No products found matching '{query}'"
                formatted = "\n".join([
                    f"Product: {p.get('name', 'N/A')} (Code: {p.get('code', 'N/A')}, ID: {p.get('id', 'N/A')}), "
                    f"Price: {p.get('price', 'N/A')}, "
                    f"Category: {p.get('category', 'N/A')}"
                    for p in products[:10]
                ])
                return f"Products found ({len(products)} total):\n{formatted}"
        return json.dumps(result, indent=2)
    
    def get_product_by_id(self, product_id: str) -> str:
        """
        Get detailed information about a specific product by ID.
        Use this when asked about a specific product.
        Input should be the product ID number.
        """
        try:
            pid = int(product_id.strip())
        except ValueError:
            return f"Invalid product ID: {product_id}. Please provide a numeric ID."
        
        result = self._make_request("GET", f"/products/{pid}")
        if "error" in result:
            return f"Error fetching product: {result['error']}"
        
        if isinstance(result, dict) and "data" in result:
            product = result["data"]
            formatted = f"""
Product Details:
- Name: {product.get('name', 'N/A')}
- Code: {product.get('code', 'N/A')}
- ID: {product.get('id', 'N/A')}
- Price: {product.get('price', 'N/A')}
- Category: {product.get('category', 'N/A')}
- Description: {product.get('description', 'N/A')}
"""
            return formatted.strip()
        return json.dumps(result, indent=2)
    
    def get_stock_discrepancies(self, query: str) -> str:
        """
        Get inventory discrepancies - differences between expected and actual stock.
        Use this when asked about discrepancies, stock differences, or inventory issues.
        """
        result = self._make_request("GET", "/inventory/discrepancies")
        if "error" in result:
            return f"Error fetching discrepancies: {result['error']}"
        
        if isinstance(result, dict) and "data" in result:
            discrepancies = result["data"]
            if isinstance(discrepancies, list):
                if len(discrepancies) == 0:
                    return "No inventory discrepancies found. All stock levels match expected values."
                formatted = "\n".join([
                    f"Product: {d.get('productName', 'N/A')}, "
                    f"Expected: {d.get('expectedQuantity', 'N/A')}, "
                    f"Actual: {d.get('actualQuantity', 'N/A')}, "
                    f"Difference: {d.get('difference', 'N/A')}"
                    for d in discrepancies
                ])
                return f"Stock discrepancies ({len(discrepancies)} items):\n{formatted}"
        return json.dumps(result, indent=2)
    
    def get_tools(self) -> list:
        """Return list of LangChain tools"""
        return [
            Tool(
                name="get_inventory",
                func=self.get_inventory,
                description="Get current inventory levels for all products. Use when asked about stock levels, inventory status, or product availability. Input can be any query about inventory."
            ),
            Tool(
                name="get_low_stock",
                func=self.get_low_stock,
                description="Get items that are low on stock. Use when asked about low stock items, items needing restocking, or products running out. Input can be any query about low stock."
            ),
            Tool(
                name="get_out_of_stock",
                func=self.get_out_of_stock,
                description="Get items that are completely out of stock (zero inventory). Use when asked about out of stock items or products with no inventory. Input can be any query about out of stock items."
            ),
            Tool(
                name="get_inventory_stats",
                func=self.get_inventory_stats,
                description="Get inventory statistics and summary including total products, stock value, low stock count, etc. Use when asked about inventory overview, summary, statistics, or general inventory status. Input can be any query about stats or summary."
            ),
            Tool(
                name="search_products",
                func=self.search_products,
                description="Search for products by name or code. Use when asked to find, search, or look up specific products. Input should be the search term (product name or code)."
            ),
            Tool(
                name="get_product_by_id",
                func=self.get_product_by_id,
                description="Get detailed information about a specific product by its ID number. Use when asked about a specific product by ID. Input should be the product ID number as a string."
            ),
            Tool(
                name="get_stock_discrepancies",
                func=self.get_stock_discrepancies,
                description="Get inventory discrepancies - differences between expected and actual stock levels. Use when asked about discrepancies, stock differences, or inventory issues. Input can be any query about discrepancies."
            )
        ]

