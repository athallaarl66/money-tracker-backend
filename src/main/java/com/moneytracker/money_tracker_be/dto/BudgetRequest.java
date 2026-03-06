package com.moneytracker.money_tracker_be.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class BudgetRequest {

    @NotBlank(message = "Kategori tidak boleh kosong")
    private String category;

    @NotNull(message = "Limit budget tidak boleh kosong")
    @DecimalMin(value = "1000", message = "Limit minimal Rp 1.000")
    private BigDecimal limitAmount;

    // Format YYYY-MM, e.g. "2025-01"
    @NotBlank(message = "Bulan tidak boleh kosong")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "Format bulan harus YYYY-MM")
    private String budgetMonth;
}