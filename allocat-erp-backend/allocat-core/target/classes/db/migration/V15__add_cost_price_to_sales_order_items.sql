-- Add cost_price column to sales_order_items for profit calculation
-- This stores the cost price at the time of sale

ALTER TABLE sales_order_items 
ADD COLUMN IF NOT EXISTS cost_price DECIMAL(10, 2) DEFAULT 0.00;

-- Add index for profit report queries
CREATE INDEX IF NOT EXISTS idx_sales_order_items_cost_price 
ON sales_order_items(cost_price);

-- Add comment for documentation
COMMENT ON COLUMN sales_order_items.cost_price IS 'Cost price at time of sale, used for profit calculation';


