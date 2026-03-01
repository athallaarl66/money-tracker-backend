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
public class CategorySummaryResponse {
    private String category;
    private BigDecimal totalAmount;
    private Long transactionCount;
    private Double percentage; // dari total expense, dihitung di service layer
}