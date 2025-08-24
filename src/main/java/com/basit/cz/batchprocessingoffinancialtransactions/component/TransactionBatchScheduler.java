package com.basit.cz.batchprocessingoffinancialtransactions.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TransactionBatchScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TransactionBatchScheduler.class);

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job processTransactionsJob;

    // Run every day at 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void runDailyTransactionProcessing() {
        runBatchJob("Daily Scheduled Processing");
    }

    // Run every 4 hours for incremental processing
    @Scheduled(cron = "0 0 */4 * * ?")
    public void runIncrementalProcessing() {
        runBatchJob("Incremental Processing");
    }

    private void runBatchJob(String trigger) {
        try {
            logger.info("Starting batch job - Trigger: {}", trigger);

            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("startTime", System.currentTimeMillis())
                    .addString("trigger", trigger)
                    .toJobParameters();

            jobLauncher.run(processTransactionsJob, jobParameters);

        } catch (Exception e) {
            logger.error("Error running batch job with trigger {}: ", trigger, e);
        }
    }
}