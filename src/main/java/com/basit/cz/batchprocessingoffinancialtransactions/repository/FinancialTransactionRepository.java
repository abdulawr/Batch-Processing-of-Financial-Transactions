package com.basit.cz.batchprocessingoffinancialtransactions.repository;

import com.basit.cz.batchprocessingoffinancialtransactions.config.TransactionStatus;
import com.basit.cz.batchprocessingoffinancialtransactions.models.FinancialTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, Long> {

    // Count methods for reporting
    @Query("SELECT COUNT(t) FROM FinancialTransaction t WHERE t.status = :status")
    long countByStatus(@Param("status") TransactionStatus status);

    @Query("SELECT SUM(t.amount) FROM FinancialTransaction t WHERE t.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") TransactionStatus status);

    // Find transactions by status
    List<FinancialTransaction> findByStatus(TransactionStatus status);

    // Find transactions by fraud score threshold
    @Query("SELECT t FROM FinancialTransaction t WHERE t.fraudScore >= :threshold")
    List<FinancialTransaction> findByFraudScoreGreaterThanEqual(@Param("threshold") Double threshold);

    // Find transactions by date range
    @Query("SELECT t FROM FinancialTransaction t WHERE t.timestamp BETWEEN :startDate AND :endDate")
    List<FinancialTransaction> findByTimestampBetween(@Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    // Find transactions by account number
    List<FinancialTransaction> findByAccountNumber(String accountNumber);

    // Check for duplicate transaction ID
    boolean existsByTransactionId(String transactionId);

    // Dashboard statistics
    @Query("SELECT t.status, COUNT(t), SUM(t.amount) FROM FinancialTransaction t GROUP BY t.status")
    List<Object[]> getStatusSummary();
}
