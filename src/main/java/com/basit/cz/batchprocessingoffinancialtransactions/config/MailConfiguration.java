package com.basit.cz.batchprocessingoffinancialtransactions.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MailConfiguration.class);

    /**
     * Create a JavaMailSender bean only if notifications are enabled
     * and mail configuration is provided
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "app.notification",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = false
    )
    public JavaMailSender javaMailSender() {
        logger.info("Creating JavaMailSender bean - notifications are enabled");

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        // Set default values that work for development/testing
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        // These should be overridden by application.yml properties
        mailSender.setUsername("your-email@gmail.com");
        mailSender.setPassword("your-app-password");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false"); // Set to true for debugging

        logger.info("JavaMailSender configured with host: {} and port: {}",
                mailSender.getHost(), mailSender.getPort());

        return mailSender;
    }

    /**
     * Fallback bean when mail is not configured
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "app.notification",
            name = "enabled",
            havingValue = "false",
            matchIfMissing = true
    )
    public JavaMailSender dummyMailSender() {
        logger.info("Creating dummy JavaMailSender - notifications are disabled");
        return new DummyJavaMailSender();
    }

    /**
     * Dummy implementation that doesn't actually send emails
     */
    private static class DummyJavaMailSender extends JavaMailSenderImpl {
        private static final Logger logger = LoggerFactory.getLogger(DummyJavaMailSender.class);

        @Override
        public void send(org.springframework.mail.SimpleMailMessage simpleMessage) {
            logger.info("DUMMY MAIL SENDER - Would send email to: {} with subject: {}",
                    simpleMessage.getTo(), simpleMessage.getSubject());
        }

        @Override
        public void send(org.springframework.mail.SimpleMailMessage... simpleMessages) {
            for (org.springframework.mail.SimpleMailMessage message : simpleMessages) {
                send(message);
            }
        }
    }
}