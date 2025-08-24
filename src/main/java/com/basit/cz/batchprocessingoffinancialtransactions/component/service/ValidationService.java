package com.basit.cz.batchprocessingoffinancialtransactions.component.service;

import com.basit.cz.batchprocessingoffinancialtransactions.models.FinancialTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class ValidationService {

    private static final Logger logger = LoggerFactory.getLogger(ValidationService.class);
    private static final List<String> VALID_TRANSACTION_TYPES =
            Arrays.asList("DEBIT", "CREDIT", "TRANSFER", "PAYMENT", "DEPOSIT", "WITHDRAWAL");

    public boolean validateTransaction(FinancialTransaction transaction) {
        StringBuilder errors = new StringBuilder();

        // Check required fields
        if (isNullOrEmpty(transaction.getTransactionId())) {
            errors.append("Transaction ID is required. ");
        }

        if (isNullOrEmpty(transaction.getAccountNumber())) {
            errors.append("Account number is required. ");
        }

        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.append("Amount must be positive. ");
        }

        if (!VALID_TRANSACTION_TYPES.contains(transaction.getTransactionType())) {
            errors.append("Invalid transaction type: " + transaction.getTransactionType() + ". ");
        }

        if (transaction.getTimestamp() == null) {
            errors.append("Timestamp is required. ");
        } else if (transaction.getTimestamp().isAfter(LocalDateTime.now())) {
            errors.append("Future-dated transactions not allowed. ");
        }

        // Business rules validation
        if (transaction.getAmount() != null &&
                transaction.getAmount().compareTo(new BigDecimal("1000000")) > 0) {
            errors.append("Transaction amount exceeds maximum limit. ");
        }

        // Account number format validation
        if (transaction.getAccountNumber() != null &&
                !transaction.getAccountNumber().matches("^ACC\\d{3,10}$")) {
            errors.append("Invalid account number format. ");
        }

        if (errors.length() > 0) {
            transaction.setErrorMessage(errors.toString().trim());
            logger.warn("Validation failed for transaction {}: {}",
                    transaction.getTransactionId(), errors.toString());
            return false;
        }

        return true;
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}


