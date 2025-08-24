package com.basit.cz.batchprocessingoffinancialtransactions.component;

import com.basit.cz.batchprocessingoffinancialtransactions.models.FinancialTransaction;
import com.basit.cz.batchprocessingoffinancialtransactions.repository.FinancialTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class TransactionWriter implements ItemWriter<FinancialTransaction> {

    private static final Logger logger = LoggerFactory.getLogger(TransactionWriter.class);

    @Autowired
    private FinancialTransactionRepository repository;

    @Override
    public void write(Chunk<? extends FinancialTransaction> chunk) throws Exception {
        try {
            logger.debug("Writing {} transactions to database", chunk.size());
            repository.saveAll(chunk.getItems());
            logger.debug("Successfully saved {} transactions", chunk.size());
        } catch (Exception e) {
            logger.error("Error writing transactions to database: ", e);
            throw e;
        }
    }
}
