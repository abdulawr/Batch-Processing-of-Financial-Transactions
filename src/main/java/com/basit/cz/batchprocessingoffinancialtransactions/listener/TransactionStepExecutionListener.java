package com.basit.cz.batchprocessingoffinancialtransactions.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionStepExecutionListener implements StepExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(StepExecutionListener.class);

    @Override
    public void beforeStep(StepExecution stepExecution) {
        logger.info("Step {} started", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        logger.info("Step {} completed. Read: {}, Written: {}, Skipped: {}",
                stepExecution.getStepName(),
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getSkipCount());

        if (stepExecution.getSkipCount() > 0) {
            logger.warn("Step had {} skipped items", stepExecution.getSkipCount());
        }

        return stepExecution.getExitStatus();
    }
}
