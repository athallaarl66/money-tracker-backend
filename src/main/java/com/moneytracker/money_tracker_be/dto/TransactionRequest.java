package com.moneytracker.money_tracker_be.dto;

import com.moneytracker.money_tracker_be.entity.Transaction.TransactionType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionRequest {
    private String description;
    private BigDecimal amount;
    private TransactionType transactionType;
    private String category;
    private LocalDate transactionDate;
}