CREATE TABLE IF NOT EXISTS financial_transactions (
                                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                      transaction_id VARCHAR(255) NOT NULL UNIQUE,
    account_number VARCHAR(255) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    description VARCHAR(1000),
    timestamp TIMESTAMP NOT NULL,
    merchant_id VARCHAR(255),
    status VARCHAR(20) DEFAULT 'PENDING',
    fraud_score DECIMAL(5,2),
    error_message VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );


CREATE INDEX idx_transaction_status ON financial_transactions(status);
CREATE INDEX idx_transaction_timestamp ON financial_transactions(timestamp);
CREATE INDEX idx_account_number ON financial_transactions(account_number);
CREATE INDEX idx_fraud_score ON financial_transactions(fraud_score);