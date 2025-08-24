package com.basit.cz.batchprocessingoffinancialtransactions.component;

import com.basit.cz.batchprocessingoffinancialtransactions.component.service.FraudDetectionService;
import com.basit.cz.batchprocessingoffinancialtransactions.component.service.ValidationService;
import com.basit.cz.batchprocessingoffinancialtransactions.config.ApplicationProperties;
import com.basit.cz.batchprocessingoffinancialtransactions.config.TransactionStatus;
import com.basit.cz.batchprocessingoffinancialtransactions.models.FinancialTransaction;
import com.basit.cz.batchprocessingoffinancialtransactions.repository.FinancialTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransactionProcessor implements ItemProcessor<FinancialTransaction, FinancialTransaction> {

    private static final Logger logger = LoggerFactory.getLogger(TransactionProcessor.class);

    @Autowired
    private ApplicationProperties appProperties;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private FraudDetectionService fraudDetectionService;

    @Autowired
    private FinancialTransactionRepository repository;

    @Override
    public FinancialTransaction process(FinancialTransaction transaction) throws Exception {
        logger.debug("Processing transaction: {}", transaction.getTransactionId());

        try {
            // Check for duplicates
            if (repository.existsByTransactionId(transaction.getTransactionId())) {
                transaction.setStatus(TransactionStatus.INVALID);
                transaction.setErrorMessage("Duplicate transaction ID");
                return transaction;
            }

            // Basic validation
            if (!validationService.validateTransaction(transaction)) {
                transaction.setStatus(TransactionStatus.INVALID);
                return transaction;
            }

            // Fraud detection
            double fraudScore = fraudDetectionService.calculateFraudScore(transaction);
            transaction.setFraudScore(fraudScore);

            if (fraudScore > 0.7) {
                transaction.setStatus(TransactionStatus.FRAUDULENT);
                logger.warn("Fraudulent transaction detected: {} with score: {}",
                        transaction.getTransactionId(), fraudScore);
                return transaction;
            }

            transaction.setStatus(TransactionStatus.VALID);
            logger.debug("Transaction validated: {}", transaction.getTransactionId());

        } catch (Exception e) {
            logger.error("Error processing transaction {}: {}",
                    transaction.getTransactionId(), e.getMessage());
            transaction.setStatus(TransactionStatus.INVALID);
            transaction.setErrorMessage("Processing error: " + e.getMessage());
        }

        return transaction;
    }
}
