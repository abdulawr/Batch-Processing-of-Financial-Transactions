package com.basit.cz.batchprocessingoffinancialtransactions.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {

    private Batch batch = new Batch();
    private Notification notification = new Notification();

    public static class Batch {
        private int chunkSize = 1000;
        private int skipLimit = 100;
        private double fraudThreshold = 10000.00;
        private String inputFile = "classpath:data/transactions.csv";
        private String outputDirectory = "./reports";

        // Getters and setters
        public int getChunkSize() { return chunkSize; }
        public void setChunkSize(int chunkSize) { this.chunkSize = chunkSize; }

        public int getSkipLimit() { return skipLimit; }
        public void setSkipLimit(int skipLimit) { this.skipLimit = skipLimit; }

        public double getFraudThreshold() { return fraudThreshold; }
        public void setFraudThreshold(double fraudThreshold) { this.fraudThreshold = fraudThreshold; }

        public String getInputFile() { return inputFile; }
        public void setInputFile(String inputFile) { this.inputFile = inputFile; }

        public String getOutputDirectory() { return outputDirectory; }
        public void setOutputDirectory(String outputDirectory) { this.outputDirectory = outputDirectory; }
    }

    public static class Notification {
        private boolean enabled = true;
        private String emailRecipients = "admin@company.com";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public String getEmailRecipients() { return emailRecipients; }
        public void setEmailRecipients(String emailRecipients) { this.emailRecipients = emailRecipients; }
    }

    public Batch getBatch() { return batch; }
    public void setBatch(Batch batch) { this.batch = batch; }

    public Notification getNotification() { return notification; }
    public void setNotification(Notification notification) { this.notification = notification; }
}