package com.moneytracker.money_tracker_be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceResponse {
    private Long accountId;
    private String accountName;
    private String accountType;
    private BigDecimal initialBalance;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal currentBalance; // initialBalance + totalIncome - totalExpense
}