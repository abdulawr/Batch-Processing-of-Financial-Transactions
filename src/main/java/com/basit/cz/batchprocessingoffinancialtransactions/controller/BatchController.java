package com.basit.cz.batchprocessingoffinancialtransactions.controller;

import com.basit.cz.batchprocessingoffinancialtransactions.config.TransactionStatus;
import com.basit.cz.batchprocessingoffinancialtransactions.repository.FinancialTransactionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/batch")
public class BatchController {

    private static final Logger logger = LoggerFactory.getLogger(BatchController.class);

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job processTransactionsJob;

    @Autowired
    private FinancialTransactionRepository repository;

    @PostMapping("/run")
    @Operation(summary = "Run the batch job for processing financial transactions", description = "Manually trigger the batch job to process financial transactions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Batch job started successfully"),
            @ApiResponse(responseCode = "500", description = "Failed to start the batch job")
    })
    public ResponseEntity<Map<String, Object>> runBatchJob() {
        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("Manual batch job execution requested");

            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("startTime", System.currentTimeMillis())
                    .addString("trigger", "Manual Execution")
                    .toJobParameters();

            jobLauncher.run(processTransactionsJob, jobParameters);

            response.put("status", "success");
            response.put("message", "Batch job started successfully");

        } catch (Exception e) {
            logger.error("Error running manual batch job: ", e);
            response.put("status", "error");
            response.put("message", "Failed to start batch job: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    @Operation(summary = "Get the current status of the batch job", description = "Fetches the current status of the financial batch processing job.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved batch job status"),
            @ApiResponse(responseCode = "500", description = "Failed to retrieve batch job status")
    })
    public ResponseEntity<Map<String, Object>> getBatchStatus() {
        Map<String, Object> response = new HashMap<>();

        try {
            long validCount = repository.countByStatus(TransactionStatus.VALID);
            long invalidCount = repository.countByStatus(TransactionStatus.INVALID);
            long fraudulentCount = repository.countByStatus(TransactionStatus.FRAUDULENT);
            long pendingCount = repository.countByStatus(TransactionStatus.PENDING);

            response.put("validTransactions", validCount);
            response.put("invalidTransactions", invalidCount);
            response.put("fraudulentTransactions", fraudulentCount);
            response.put("pendingTransactions", pendingCount);
            response.put("totalProcessed", validCount + invalidCount + fraudulentCount);

        } catch (Exception e) {
            logger.error("Error getting batch status: ", e);
            response.put("error", "Failed to get batch status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }

        return ResponseEntity.ok(response);
    }
}