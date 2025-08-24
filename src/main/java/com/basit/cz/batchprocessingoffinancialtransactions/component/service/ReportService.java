package com.basit.cz.batchprocessingoffinancialtransactions.component.service;

import com.basit.cz.batchprocessingoffinancialtransactions.config.ApplicationProperties;
import com.basit.cz.batchprocessingoffinancialtransactions.config.TransactionStatus;
import com.basit.cz.batchprocessingoffinancialtransactions.models.FinancialTransaction;
import com.basit.cz.batchprocessingoffinancialtransactions.repository.FinancialTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    private FinancialTransactionRepository repository;

    @Autowired
    private ApplicationProperties appProperties;

    public String generateSummaryReport(JobExecution jobExecution) throws IOException {
        logger.info("Generating summary report for job execution: {}", jobExecution.getId());

        // Create output directory if it doesn't exist
        File outputDir = new File(appProperties.getBatch().getOutputDirectory());
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String reportFileName = String.format("%s/transaction_report_%s.txt",
                appProperties.getBatch().getOutputDirectory(),
                timestamp);

        // Gather statistics
        long validCount = repository.countByStatus(TransactionStatus.VALID);
        long invalidCount = repository.countByStatus(TransactionStatus.INVALID);
        long fraudulentCount = repository.countByStatus(TransactionStatus.FRAUDULENT);
        BigDecimal totalValidAmount = repository.sumAmountByStatus(TransactionStatus.VALID);

        try (FileWriter writer = new FileWriter(reportFileName)) {
            // Report header
            writer.write("FINANCIAL TRANSACTION PROCESSING REPORT\n");
            writer.write("======================================\n");
            writer.write("Report Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
            writer.write("Job Execution ID: " + jobExecution.getId() + "\n");
            writer.write("Job Status: " + jobExecution.getStatus() + "\n");
            writer.write("Start Time: " + jobExecution.getStartTime() + "\n");
            writer.write("End Time: " + jobExecution.getEndTime() + "\n");

            // Calculate and write duration safely
            String duration = calculateJobDuration(jobExecution);
            writer.write("Duration: " + duration + "\n\n");

            // Summary statistics
            writer.write("TRANSACTION SUMMARY\n");
            writer.write("==================\n");
            writer.write("Valid Transactions: " + validCount + "\n");
            writer.write("Invalid Transactions: " + invalidCount + "\n");
            writer.write("Fraudulent Transactions: " + fraudulentCount + "\n");
            writer.write("Total Processed: " + (validCount + invalidCount + fraudulentCount) + "\n");
            writer.write("Total Valid Amount: $" + (totalValidAmount != null ? totalValidAmount : BigDecimal.ZERO) + "\n\n");

            // Step execution details
            writer.write("STEP EXECUTION DETAILS\n");
            writer.write("=====================\n");
            jobExecution.getStepExecutions().forEach(stepExecution -> {
                try {
                    writer.write("Step: " + stepExecution.getStepName() + "\n");
                    writer.write("  Read Count: " + stepExecution.getReadCount() + "\n");
                    writer.write("  Write Count: " + stepExecution.getWriteCount() + "\n");
                    writer.write("  Skip Count: " + stepExecution.getSkipCount() + "\n");
                    writer.write("  Status: " + stepExecution.getStatus() + "\n\n");
                } catch (IOException e) {
                    logger.error("Error writing step details: ", e);
                }
            });

            // Error details
            writeErrorDetails(writer);

            // Fraud details
            writeFraudDetails(writer);
        }

        logger.info("Summary report generated: {}", reportFileName);
        return reportFileName;
    }

    /**
     * Calculate job duration handling both Date and LocalDateTime types
     */
    private String calculateJobDuration(JobExecution jobExecution) {
        try {
            Object startTime = jobExecution.getStartTime();
            Object endTime = jobExecution.getEndTime();

            if (startTime == null || endTime == null) {
                return "Unknown";
            }

            LocalDateTime start = convertToLocalDateTime(startTime);
            LocalDateTime end = convertToLocalDateTime(endTime);

            if (start == null || end == null) {
                return "Unknown";
            }

            Duration duration = Duration.between(start, end);
            return formatDuration(duration);

        } catch (Exception e) {
            logger.warn("Error calculating job duration: {}", e.getMessage());
            return "Unknown";
        }
    }

    /**
     * Convert various time objects to LocalDateTime
     */
    private LocalDateTime convertToLocalDateTime(Object timeObject) {
        if (timeObject == null) {
            return null;
        }

        try {
            if (timeObject instanceof LocalDateTime) {
                return (LocalDateTime) timeObject;
            } else if (timeObject instanceof java.util.Date) {
                return ((java.util.Date) timeObject).toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
            } else if (timeObject instanceof java.sql.Timestamp) {
                return ((java.sql.Timestamp) timeObject).toLocalDateTime();
            } else {
                logger.warn("Unknown time object type: {}", timeObject.getClass());
                return null;
            }
        } catch (Exception e) {
            logger.warn("Error converting time object: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Format duration into human-readable string
     */
    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }

    private void writeErrorDetails(FileWriter writer) throws IOException {
        List<FinancialTransaction> invalidTransactions = repository.findByStatus(TransactionStatus.INVALID);

        if (!invalidTransactions.isEmpty()) {
            writer.write("INVALID TRANSACTIONS DETAILS\n");
            writer.write("===========================\n");
            for (FinancialTransaction transaction : invalidTransactions) {
                writer.write("Transaction ID: " + transaction.getTransactionId() + "\n");
                writer.write("  Account: " + transaction.getAccountNumber() + "\n");
                writer.write("  Amount: $" + transaction.getAmount() + "\n");
                writer.write("  Error: " + transaction.getErrorMessage() + "\n");
                writer.write("  Timestamp: " + transaction.getTimestamp() + "\n\n");
            }
        }
    }

    private void writeFraudDetails(FileWriter writer) throws IOException {
        List<FinancialTransaction> fraudulentTransactions = repository.findByStatus(TransactionStatus.FRAUDULENT);

        if (!fraudulentTransactions.isEmpty()) {
            writer.write("FRAUDULENT TRANSACTIONS DETAILS\n");
            writer.write("==============================\n");
            for (FinancialTransaction transaction : fraudulentTransactions) {
                writer.write("Transaction ID: " + transaction.getTransactionId() + "\n");
                writer.write("  Account: " + transaction.getAccountNumber() + "\n");
                writer.write("  Amount: $" + transaction.getAmount() + "\n");
                writer.write("  Fraud Score: " + transaction.getFraudScore() + "\n");
                writer.write("  Merchant: " + transaction.getMerchantId() + "\n");
                writer.write("  Timestamp: " + transaction.getTimestamp() + "\n\n");
            }
        }
    }
}