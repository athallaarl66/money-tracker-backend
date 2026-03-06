package com.moneytracker.money_tracker_be.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class BudgetResponse {

    private Long id;
    private String category;
    private BigDecimal limitAmount;

    // Total pengeluaran di kategori ini bulan tersebut
    private BigDecimal spentAmount;

    // Persentase pemakaian — spentAmount / limitAmount * 100
    // Bisa lebih dari 100 kalau udah over budget
    private double percentage;

    // Sisa budget — bisa negatif kalau over
    private BigDecimal remainingAmount;

    private String budgetMonth;
}