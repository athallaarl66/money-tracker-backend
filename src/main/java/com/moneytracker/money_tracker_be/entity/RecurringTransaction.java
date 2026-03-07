package com.moneytracker.money_tracker_be.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "recurring_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private Transaction.TransactionType transactionType;

    private String category;

    // Seberapa sering transaksi ini diulang
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Frequency frequency;

    // Tanggal kapan scheduler akan jalankan transaksi ini berikutnya
    @Column(name = "next_run_date", nullable = false)
    private LocalDate nextRunDate;

    // Bisa di-pause tanpa harus hapus
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public enum Frequency {
        DAILY,    // tiap hari
        WEEKLY,   // tiap minggu
        MONTHLY,  // tiap bulan — paling umum
        YEARLY    // tiap tahun
    }
}