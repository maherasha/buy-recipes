-- Add quantity column to cart_items table
ALTER TABLE cart_items 
ADD COLUMN quantity INT NOT NULL DEFAULT 1;

-- Update existing records to have quantity = 1 (they are already individual items)
UPDATE cart_items SET quantity = 1 WHERE quantity IS NULL;