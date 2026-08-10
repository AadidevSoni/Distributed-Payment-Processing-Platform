CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    from_wallet_id UUID NOT NULL,
    to_wallet_id UUID NOT NULL,
    amount NUMERIC(19,4) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    CONSTRAINT fk_transactions_from_wallet FOREIGN KEY (from_wallet_id) REFERENCES wallets (id),
    CONSTRAINT fk_transactions_to_wallet FOREIGN KEY (to_wallet_id) REFERENCES wallets (id),
    CONSTRAINT chk_transactions_amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_transactions_from_wallet ON transactions (from_wallet_id);
CREATE INDEX idx_transactions_to_wallet ON transactions (to_wallet_id);