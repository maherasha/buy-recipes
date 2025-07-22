-- Drop existing carts table to recreate with new schema
DROP TABLE IF EXISTS carts;

-- Create new carts table with simplified schema
CREATE TABLE carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    total_amount INT NOT NULL DEFAULT 0
);

-- Create products table
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price_in_cents INT NOT NULL
);

-- Create cart_items table to link carts and products
CREATE TABLE cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Create recipes table
CREATE TABLE recipes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

-- Create recipe_products table to link recipes and products with quantities
CREATE TABLE recipe_products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipe_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Insert sample data
INSERT INTO products (name, price_in_cents) VALUES 
('Tomatoes', 399),
('Onions', 250),
('Bread', 499),
('Milk', 299),
('Chicken Breast', 1299),
('Olive Oil', 599),
('Garlic', 150),
('Pasta', 399);

INSERT INTO recipes (name) VALUES 
('Pasta with Tomato Sauce'),
('Chicken and Vegetables'),
('Simple Bread and Milk');

INSERT INTO recipe_products (recipe_id, product_id, quantity) VALUES 
-- Pasta with Tomato Sauce
(1, 1, 2), -- 2 Tomatoes
(1, 2, 1), -- 1 Onions
(1, 6, 1), -- 1 Olive Oil
(1, 7, 2), -- 2 Garlic
(1, 8, 1), -- 1 Pasta

-- Chicken and Vegetables
(2, 5, 1), -- 1 Chicken Breast
(2, 1, 1), -- 1 Tomatoes
(2, 2, 1), -- 1 Onions
(2, 6, 1), -- 1 Olive Oil

-- Simple Bread and Milk
(3, 3, 1), -- 1 Bread
(3, 4, 1); -- 1 Milk

-- Insert sample carts
INSERT INTO carts (total_amount) VALUES 
(0),
(0),
(0);