package com.basit.cz.batchprocessingoffinancialtransactions.config;

import com.basit.cz.batchprocessingoffinancialtransactions.component.TransactionCsvReader;
import com.basit.cz.batchprocessingoffinancialtransactions.component.TransactionProcessor;
import com.basit.cz.batchprocessingoffinancialtransactions.component.TransactionWriter;
import com.basit.cz.batchprocessingoffinancialtransactions.listener.JobCompletionNotificationListener;
import com.basit.cz.batchprocessingoffinancialtransactions.listener.TransactionStepExecutionListener;
import com.basit.cz.batchprocessingoffinancialtransactions.models.FinancialTransaction;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfiguration {

    @Autowired
    private ApplicationProperties appProperties;

    @Autowired
    private TransactionCsvReader csvReader;

    @Autowired
    private TransactionProcessor processor;

    @Autowired
    private TransactionWriter writer;

    @Autowired
    private JobCompletionNotificationListener jobCompletionListener;

    @Autowired
    private TransactionStepExecutionListener stepExecutionListener;

    @Bean
    public Job processTransactionsJob(JobRepository jobRepository, Step processTransactionsStep) {
        return new JobBuilder("processTransactionsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobCompletionListener)
                .start(processTransactionsStep)
                .build();
    }

    @Bean
    public Step processTransactionsStep(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager) {
        return new StepBuilder("processTransactionsStep", jobRepository)
                .<FinancialTransaction, FinancialTransaction>chunk(appProperties.getBatch().getChunkSize(), transactionManager)
                .reader(csvReader.csvFileItemReader())
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipLimit(appProperties.getBatch().getSkipLimit())
                .skip(Exception.class)
                .skipPolicy(new AlwaysSkipItemSkipPolicy())
                .listener(stepExecutionListener)
                .build();
    }
}