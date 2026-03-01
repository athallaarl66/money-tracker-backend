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
public class MonthlyTrendResponse {
    private String month;       // format: "2025-01"
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal netBalance;
}