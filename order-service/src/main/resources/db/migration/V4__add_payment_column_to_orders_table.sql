ALTER TABLE orders ADD COLUMN payment_id UUID;
CREATE UNIQUE INDEX idx_orders_payment_id ON orders (payment_id) WHERE payment_id IS NOT NULL;
