CREATE TABLE fraud_checks (
    id UUID PRIMARY KEY,
    transaction_id UUID,
    from_wallet_id UUID NOT NULL,
    risk_score INT NOT NULL,
    decision VARCHAR(20) NOT NULL,
    reasons TEXT,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_fraud_checks_from_wallet ON fraud_checks (from_wallet_id);