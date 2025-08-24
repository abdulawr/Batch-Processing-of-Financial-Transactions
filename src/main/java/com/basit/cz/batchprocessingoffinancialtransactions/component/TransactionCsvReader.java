package com.basit.cz.batchprocessingoffinancialtransactions.component;

import com.basit.cz.batchprocessingoffinancialtransactions.config.ApplicationProperties;
import com.basit.cz.batchprocessingoffinancialtransactions.models.FinancialTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class TransactionCsvReader {

    private static final Logger logger = LoggerFactory.getLogger(TransactionCsvReader.class);

    @Autowired
    private ApplicationProperties appProperties;

    @Autowired
    private ResourceLoader resourceLoader;

    public FlatFileItemReader<FinancialTransaction> csvFileItemReader() {
        FlatFileItemReader<FinancialTransaction> reader = new FlatFileItemReader<>();

        try {
            Resource resource = resourceLoader.getResource(appProperties.getBatch().getInputFile());
            reader.setResource(resource);
            reader.setName("transactionCsvReader");
            reader.setLinesToSkip(1); // Skip header
            reader.setLineMapper(lineMapper());

            logger.info("CSV Reader configured with file: {}", appProperties.getBatch().getInputFile());
        } catch (Exception e) {
            logger.error("Error configuring CSV reader: ", e);
            throw new RuntimeException("Failed to configure CSV reader", e);
        }

        return reader;
    }

    private LineMapper<FinancialTransaction> lineMapper() {
        DefaultLineMapper<FinancialTransaction> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("transactionId", "accountNumber", "amount",
                "transactionType", "description", "timestamp", "merchantId");

        BeanWrapperFieldSetMapper<FinancialTransaction> fieldSetMapper =
                new BeanWrapperFieldSetMapper<FinancialTransaction>() {
                    @Override
                    public FinancialTransaction mapFieldSet(org.springframework.batch.item.file.transform.FieldSet fieldSet) {
                        FinancialTransaction transaction = new FinancialTransaction();

                        try {
                            transaction.setTransactionId(fieldSet.readString("transactionId"));
                            transaction.setAccountNumber(fieldSet.readString("accountNumber"));
                            transaction.setAmount(fieldSet.readBigDecimal("amount"));
                            transaction.setTransactionType(fieldSet.readString("transactionType"));
                            transaction.setDescription(fieldSet.readString("description"));

                            // Parse timestamp
                            String timestampStr = fieldSet.readString("timestamp");
                            if (timestampStr != null && !timestampStr.trim().isEmpty()) {
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                                transaction.setTimestamp(LocalDateTime.parse(timestampStr, formatter));
                            }

                            transaction.setMerchantId(fieldSet.readString("merchantId"));

                        } catch (Exception e) {
                            logger.warn("Error parsing transaction: {}", e.getMessage());
                            // Set error message and continue processing
                            transaction.setErrorMessage("Parse error: " + e.getMessage());
                        }

                        return transaction;
                    }
                };
        fieldSetMapper.setTargetType(FinancialTransaction.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }
}