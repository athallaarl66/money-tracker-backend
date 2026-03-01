package com.moneytracker.money_tracker_be.service;

import com.moneytracker.money_tracker_be.dto.*;
import com.moneytracker.money_tracker_be.entity.Transaction;
import com.moneytracker.money_tracker_be.repository.AccountRepository;
import com.moneytracker.money_tracker_be.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    // ================================================================
    // 1. SUMMARY
    // ================================================================
    public SummaryResponse getSummary(Long userId, Integer year, Integer month) {
        if (year != null && month != null) {
            return getSummaryByMonth(userId, year, month);
        }
        return getAllTimeSummary(userId);
    }

    private SummaryResponse getAllTimeSummary(Long userId) {
        BigDecimal totalIncome = transactionRepository
                .sumByUserIdAndType(userId, Transaction.TransactionType.INCOME);
        BigDecimal totalExpense = transactionRepository
                .sumByUserIdAndType(userId, Transaction.TransactionType.EXPENSE);

        return SummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netBalance(totalIncome.subtract(totalExpense))
                .period("all-time")
                .build();
    }

    private SummaryResponse getSummaryByMonth(Long userId, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59);

        BigDecimal totalIncome = transactionRepository
                .sumByUserIdAndTypeAndDateRange(userId, Transaction.TransactionType.INCOME, start, end);
        BigDecimal totalExpense = transactionRepository
                .sumByUserIdAndTypeAndDateRange(userId, Transaction.TransactionType.EXPENSE, start, end);

        return SummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netBalance(totalIncome.subtract(totalExpense))
                .period(String.format("%d-%02d", year, month))
                .build();
    }

    // ================================================================
    // 2. CATEGORY BREAKDOWN
    // ================================================================
    public List<CategorySummaryResponse> getCategoryBreakdown(
            Long userId, Integer year, Integer month) {

        List<Object[]> rawData;

        if (year != null && month != null) {
            YearMonth ym = YearMonth.of(year, month);
            LocalDateTime start = ym.atDay(1).atStartOfDay();
            LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59);
            rawData = transactionRepository
                    .findExpenseByCategoryAndDateRange(userId, start, end);
        } else {
            rawData = transactionRepository.findExpenseByCategory(userId);
        }

        // Hitung grand total untuk persentase
        BigDecimal grandTotal = rawData.stream()
                .map(row -> (BigDecimal) row[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return rawData.stream().map(row -> {
            BigDecimal amount = (BigDecimal) row[1];
            Double percentage = grandTotal.compareTo(BigDecimal.ZERO) > 0
                    ? amount.divide(grandTotal, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue()
                    : 0.0;

            return CategorySummaryResponse.builder()
                    .category(row[0] != null ? (String) row[0] : "Uncategorized")
                    .totalAmount(amount)
                    .transactionCount((Long) row[2])
                    .percentage(percentage)
                    .build();
        }).collect(Collectors.toList());
    }

    // ================================================================
    // 3. MONTHLY TREND
    // ================================================================
    public List<MonthlyTrendResponse> getMonthlyTrend(Long userId, int months) {
        LocalDateTime startDate = LocalDateTime.now()
                .minusMonths(months)
                .withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        List<Object[]> rawData = transactionRepository
                .findMonthlyTrend(userId, startDate);

        return rawData.stream().map(row -> {
            BigDecimal income  = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
            BigDecimal expense = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;

            return MonthlyTrendResponse.builder()
                    .month((String) row[0])
                    .income(income)
                    .expense(expense)
                    .netBalance(income.subtract(expense))
                    .build();
        }).collect(Collectors.toList());
    }

    // ================================================================
    // 4. ACCOUNT BALANCES
    // ================================================================
    public List<AccountBalanceResponse> getAccountBalances(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(account -> {
                    BigDecimal totalIncome = transactionRepository
                            .sumByAccountIdAndType(
                                    account.getId(),
                                    Transaction.TransactionType.INCOME);
                    BigDecimal totalExpense = transactionRepository
                            .sumByAccountIdAndType(
                                    account.getId(),
                                    Transaction.TransactionType.EXPENSE);
                    BigDecimal initialBalance = account.getInitialBalance() != null
                            ? account.getInitialBalance()
                            : BigDecimal.ZERO;

                    return AccountBalanceResponse.builder()
                            .accountId(account.getId())
                            .accountName(account.getName())
                            .accountType(account.getType())
                            .initialBalance(initialBalance)
                            .totalIncome(totalIncome)
                            .totalExpense(totalExpense)
                            .currentBalance(initialBalance
                                    .add(totalIncome)
                                    .subtract(totalExpense))
                            .build();
                })
                .collect(Collectors.toList());
    }
}