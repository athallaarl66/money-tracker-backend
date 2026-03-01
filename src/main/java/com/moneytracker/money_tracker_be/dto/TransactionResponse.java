package com.moneytracker.money_tracker_be.dto;

import com.moneytracker.money_tracker_be.entity.Transaction.TransactionType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {
    private Long id;
    private Long accountId;
    private String accountName;
    private String description;
    private BigDecimal amount;
    private TransactionType transactionType;
    private String category;
    private LocalDateTime transactionDate;
    private LocalDateTime createdAt;
}