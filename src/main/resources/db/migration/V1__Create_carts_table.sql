CREATE TABLE carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO carts (user_id, product_name, quantity, price) VALUES 
('user1', 'Tomatoes', 2, 3.99),
('user1', 'Onions', 1, 2.50),
('user2', 'Bread', 1, 4.99),
('user2', 'Milk', 2, 5.98),
('user3', 'Chicken', 1, 12.99);