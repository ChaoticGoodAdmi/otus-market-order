-- SQL script to create schema for the Order Service

-- Create table for orders
CREATE TABLE orders (
                        id SERIAL PRIMARY KEY,             -- Order ID (Primary Key)
                        user_id VARCHAR(255) NOT NULL,     -- User ID (Foreign Key to user service or system)
                        total_price NUMERIC(10, 2) NOT NULL, -- Total order price
                        status VARCHAR(50) NOT NULL,       -- Order status (CREATED, PAID, SHIPPED, etc.)
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Order creation timestamp
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP  -- Order update timestamp
);

-- Create table for order items
CREATE TABLE order_items (
                             id SERIAL PRIMARY KEY,             -- Order Item ID (Primary Key)
                             order_id INTEGER NOT NULL,         -- Foreign Key to the orders table
                             product_id VARCHAR(255) NOT NULL,  -- Product ID (can be linked to product service)
                             quantity INTEGER NOT NULL CHECK (quantity > 0),  -- Product quantity
                             price NUMERIC(10, 2) NOT NULL,     -- Price per product unit
                             FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);

-- Indexes for performance optimization
CREATE INDEX idx_order_user ON orders (user_id);
CREATE INDEX idx_order_status ON orders (status);
CREATE INDEX idx_order_created_at ON orders (created_at);
