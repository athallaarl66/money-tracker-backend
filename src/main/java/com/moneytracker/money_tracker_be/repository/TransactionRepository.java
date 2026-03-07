package com.moneytracker.money_tracker_be.repository;

import com.moneytracker.money_tracker_be.entity.Account;
import com.moneytracker.money_tracker_be.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccount(Account account);
    Optional<Transaction> findByIdAndAccount(Long id, Account account);
    List<Transaction> findByAccountId(Long accountId);
    List<Transaction> findByAccountUserId(Long userId);

    // ── Analytics ──

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.account.user.id = :userId AND t.transactionType = :type")
    BigDecimal sumByUserIdAndType(
            @Param("userId") Long userId,
            @Param("type") Transaction.TransactionType type);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.account.user.id = :userId AND t.transactionType = :type " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumByUserIdAndTypeAndDateRange(
            @Param("userId") Long userId,
            @Param("type") Transaction.TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT t.category, SUM(t.amount), COUNT(t) FROM Transaction t " +
            "WHERE t.account.user.id = :userId " +
            "AND t.transactionType = com.moneytracker.money_tracker_be.entity.Transaction.TransactionType.EXPENSE " +
            "GROUP BY t.category ORDER BY SUM(t.amount) DESC")
    List<Object[]> findExpenseByCategory(@Param("userId") Long userId);

    @Query("SELECT t.category, SUM(t.amount), COUNT(t) FROM Transaction t " +
            "WHERE t.account.user.id = :userId " +
            "AND t.transactionType = com.moneytracker.money_tracker_be.entity.Transaction.TransactionType.EXPENSE " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY t.category ORDER BY SUM(t.amount) DESC")
    List<Object[]> findExpenseByCategoryAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query(value =
            "SELECT TO_CHAR(t.transaction_date, 'YYYY-MM') as month, " +
                    "  SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END) as income, " +
                    "  SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END) as expense " +
                    "FROM transactions t " +
                    "JOIN accounts a ON t.account_id = a.id " +
                    "WHERE a.user_id = :userId " +
                    "  AND t.transaction_date >= :startDate " +
                    "GROUP BY TO_CHAR(t.transaction_date, 'YYYY-MM') " +
                    "ORDER BY month ASC",
            nativeQuery = true)
    List<Object[]> findMonthlyTrend(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.account.id = :accountId AND t.transactionType = :type")
    BigDecimal sumByAccountIdAndType(
            @Param("accountId") Long accountId,
            @Param("type") Transaction.TransactionType type);
}