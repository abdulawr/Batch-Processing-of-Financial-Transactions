package com.basit.cz.batchprocessingoffinancialtransactions.config;

public enum TransactionStatus {
    PENDING("Pending Processing"),
    VALID("Valid Transaction"),
    INVALID("Invalid Transaction"),
    FRAUDULENT("Fraudulent Transaction"),
    PROCESSED("Successfully Processed");

    private final String description;

    TransactionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
