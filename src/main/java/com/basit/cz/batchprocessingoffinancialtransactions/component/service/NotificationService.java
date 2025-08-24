package com.basit.cz.batchprocessingoffinancialtransactions.component.service;

import com.basit.cz.batchprocessingoffinancialtransactions.config.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private ApplicationProperties appProperties;

    public void sendCompletionNotification(JobExecution jobExecution, String reportPath) {
        if (!appProperties.getNotification().isEnabled()) {
            logger.info("Notifications are disabled");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(appProperties.getNotification().getEmailRecipients().split(","));
            message.setSubject("Financial Batch Processing Completed Successfully");

            StringBuilder body = new StringBuilder();
            body.append("The financial transaction batch job has completed successfully.\n\n");
            body.append("Job Details:\n");
            body.append("- Job ID: ").append(jobExecution.getId()).append("\n");
            body.append("- Status: ").append(jobExecution.getStatus()).append("\n");
            body.append("- Start Time: ").append(jobExecution.getStartTime()).append("\n");
            body.append("- End Time: ").append(jobExecution.getEndTime()).append("\n");

            // Calculate duration safely
            String duration = calculateJobDuration(jobExecution);
            body.append("- Duration: ").append(duration).append("\n\n");

            // Add step statistics
            jobExecution.getStepExecutions().forEach(stepExecution -> {
                body.append("Step: ").append(stepExecution.getStepName()).append("\n");
                body.append("- Read: ").append(stepExecution.getReadCount()).append("\n");
                body.append("- Written: ").append(stepExecution.getWriteCount()).append("\n");
                body.append("- Skipped: ").append(stepExecution.getSkipCount()).append("\n\n");
            });

            body.append("Detailed report has been generated at: ").append(reportPath).append("\n");

            message.setText(body.toString());

            mailSender.send(message);
            logger.info("Success notification sent to: {}", appProperties.getNotification().getEmailRecipients());

        } catch (Exception e) {
            logger.error("Failed to send completion notification: ", e);
        }
    }

    public void sendErrorNotification(JobExecution jobExecution) {
        if (!appProperties.getNotification().isEnabled()) {
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(appProperties.getNotification().getEmailRecipients().split(","));
            message.setSubject("Financial Batch Processing Failed");

            StringBuilder body = new StringBuilder();
            body.append("The financial transaction batch job has failed.\n\n");
            body.append("Job Details:\n");
            body.append("- Job ID: ").append(jobExecution.getId()).append("\n");
            body.append("- Status: ").append(jobExecution.getStatus()).append("\n");
            body.append("- Start Time: ").append(jobExecution.getStartTime()).append("\n");

            if (jobExecution.getEndTime() != null) {
                body.append("- End Time: ").append(jobExecution.getEndTime()).append("\n");
                String duration = calculateJobDuration(jobExecution);
                body.append("- Duration: ").append(duration).append("\n");
            }

            // Add failure exceptions
            jobExecution.getAllFailureExceptions().forEach(exception -> {
                body.append("- Error: ").append(exception.getMessage()).append("\n");
            });

            body.append("\nPlease check the application logs for more details.\n");

            message.setText(body.toString());

            mailSender.send(message);
            logger.info("Error notification sent to: {}", appProperties.getNotification().getEmailRecipients());

        } catch (Exception e) {
            logger.error("Failed to send error notification: ", e);
        }
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
        long millis = duration.toMillisPart();

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else if (seconds > 0) {
            return String.format("%d.%03ds", seconds, millis);
        } else {
            return String.format("%dms", millis);
        }
    }

    /**
     * Send custom notification with specific message
     */
    public void sendCustomNotification(String subject, String messageBody) {
        if (!appProperties.getNotification().isEnabled()) {
            logger.info("Notifications are disabled");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(appProperties.getNotification().getEmailRecipients().split(","));
            message.setSubject(subject);
            message.setText(messageBody);

            mailSender.send(message);
            logger.info("Custom notification sent: {}", subject);

        } catch (Exception e) {
            logger.error("Failed to send custom notification: ", e);
        }
    }

    /**
     * Send notification for high number of fraudulent transactions
     */
    public void sendFraudAlert(long fraudulentCount, String details) {
        if (!appProperties.getNotification().isEnabled()) {
            return;
        }

        String subject = String.format("FRAUD ALERT: %d Suspicious Transactions Detected", fraudulentCount);

        StringBuilder body = new StringBuilder();
        body.append("URGENT: High number of potentially fraudulent transactions detected!\n\n");
        body.append("Fraudulent Transactions Count: ").append(fraudulentCount).append("\n\n");
        body.append("Details:\n");
        body.append(details).append("\n\n");
        body.append("Please review immediately and take appropriate action.\n");
        body.append("Check the detailed fraud report for more information.\n");

        sendCustomNotification(subject, body.toString());
    }
}