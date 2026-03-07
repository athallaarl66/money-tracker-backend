package com.moneytracker.money_tracker_be.dto;

import com.moneytracker.money_tracker_be.entity.RecurringTransaction.Frequency;
import com.moneytracker.money_tracker_be.entity.Transaction.TransactionType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class RecurringTransactionResponse {

    private Long id;
    private Long accountId;
    private String accountName;
    private String description;
    private BigDecimal amount;
    private TransactionType transactionType;
    private String category;
    private Frequency frequency;
    private LocalDate nextRunDate;
    private boolean active;
    private LocalDateTime createdAt;
}