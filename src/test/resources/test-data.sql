-- Test data for integration tests
-- This will be loaded before each test method

-- Insert test products
INSERT INTO product (id, name, price_in_cents) VALUES 
(1, 'Tomatoes', 399),
(2, 'Onions', 250);

-- Insert test carts
INSERT INTO cart (id, total_amount) VALUES 
(1, 649),
(2, 798);

-- Insert test cart items
INSERT INTO cart_item (id, cart_id, product_id, quantity) VALUES 
(1, 1, 1, 1),
(2, 1, 2, 1),
(3, 2, 1, 2);

-- Insert test recipe
INSERT INTO recipe (id, name) VALUES 
(1, 'Tomato and Onion Salad');

-- Insert test recipe products
INSERT INTO recipe_product (id, recipe_id, product_id, quantity) VALUES 
(1, 1, 1, 2),
(2, 1, 2, 1);