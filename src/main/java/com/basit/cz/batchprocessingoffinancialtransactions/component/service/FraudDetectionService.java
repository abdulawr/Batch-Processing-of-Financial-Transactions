package com.basit.cz.batchprocessingoffinancialtransactions.component.service;

import com.basit.cz.batchprocessingoffinancialtransactions.config.ApplicationProperties;
import com.basit.cz.batchprocessingoffinancialtransactions.models.FinancialTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class FraudDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(FraudDetectionService.class);

    @Autowired
    private ApplicationProperties appProperties;

    public double calculateFraudScore(FinancialTransaction transaction) {
        double score = 0.0;

        try {
            // High amount transactions
            BigDecimal fraudThreshold = BigDecimal.valueOf(appProperties.getBatch().getFraudThreshold());
            if (transaction.getAmount().compareTo(fraudThreshold) > 0) {
                score += 0.3;
                logger.debug("High amount flag for transaction {}: {}",
                        transaction.getTransactionId(), transaction.getAmount());
            }

            // Weekend transactions
            if (transaction.getTimestamp().getDayOfWeek().getValue() >= 6) {
                score += 0.1;
            }

            // Late night or early morning transactions (11 PM - 6 AM)
            int hour = transaction.getTimestamp().getHour();
            if (hour >= 23 || hour <= 6) {
                score += 0.2;
            }

            // Suspicious merchant patterns
            if (transaction.getMerchantId() != null) {
                String merchantId = transaction.getMerchantId().toUpperCase();
                if (merchantId.contains("CASH") ||
                        merchantId.contains("ATM") ||
                        merchantId.contains("UNKNOWN")) {
                    score += 0.15;
                }
            }

            // Round-number amounts (potential indicator)
            if (transaction.getAmount().remainder(BigDecimal.valueOf(100)).equals(BigDecimal.ZERO)) {
                score += 0.05;
            }

            // Multiple transactions from same account (simplified check)
            if ("TRANSFER".equals(transaction.getTransactionType()) &&
                    transaction.getAmount().compareTo(new BigDecimal("5000")) > 0) {
                score += 0.1;
            }

        } catch (Exception e) {
            logger.warn("Error calculating fraud score for transaction {}: {}",
                    transaction.getTransactionId(), e.getMessage());
        }

        return Math.min(score, 1.0);
    }
}
