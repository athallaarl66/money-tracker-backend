package com.moneytracker.money_tracker_be.dto;

import com.moneytracker.money_tracker_be.entity.RecurringTransaction.Frequency;
import com.moneytracker.money_tracker_be.entity.Transaction.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RecurringTransactionRequest {

    @NotNull(message = "Account tidak boleh kosong")
    private Long accountId;

    @NotBlank(message = "Deskripsi tidak boleh kosong")
    private String description;

    @NotNull(message = "Amount tidak boleh kosong")
    @DecimalMin(value = "1", message = "Amount minimal 1")
    private BigDecimal amount;

    @NotNull(message = "Tipe transaksi tidak boleh kosong")
    private TransactionType transactionType;

    private String category;

    @NotNull(message = "Frekuensi tidak boleh kosong")
    private Frequency frequency;

    // Tanggal mulai — kapan pertama kali dijalankan
    @NotNull(message = "Tanggal mulai tidak boleh kosong")
    private LocalDate startDate;
}