CREATE TABLE IF NOT EXISTS accounts (
	id UUID PRIMARY KEY,
	customer_id VARCHAR(255) NOT NULL,
	balance NUMERIC(19, 4) NOT NULL,
	currency VARCHAR(3) NOT NULL,
	created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT uq_accounts_customer_currency UNIQUE (customer_id, currency)
);

CREATE INDEX idx_accounts_customer ON accounts (customer_id);