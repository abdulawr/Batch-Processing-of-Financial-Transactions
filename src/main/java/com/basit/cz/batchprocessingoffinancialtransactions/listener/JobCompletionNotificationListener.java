package com.basit.cz.batchprocessingoffinancialtransactions.listener;


import com.basit.cz.batchprocessingoffinancialtransactions.component.service.NotificationService;
import com.basit.cz.batchprocessingoffinancialtransactions.component.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionNotificationListener implements JobExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

    @Autowired
    private ReportService reportService;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        logger.info("Job {} started with parameters: {}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getJobParameters());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        logger.info("Job {} finished with status: {}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus());

        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            try {
                // Generate reports
                String reportPath = reportService.generateSummaryReport(jobExecution);

                // Send notifications
                notificationService.sendCompletionNotification(jobExecution, reportPath);

                logger.info("Job completion processing finished successfully");

            } catch (Exception e) {
                logger.error("Error in job completion processing: ", e);
            }
        } else {
            logger.warn("Job completed with status: {}", jobExecution.getStatus());
            notificationService.sendErrorNotification(jobExecution);
        }
    }
}
