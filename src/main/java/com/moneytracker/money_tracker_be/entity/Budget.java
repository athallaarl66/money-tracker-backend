package com.moneytracker.money_tracker_be.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Table(
        name = "budgets",
         uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "category", "budget_month"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

     @Column(nullable = false)
    private String category;

    // Limit pengeluaran per bulan untuk kategori ini
    @Column(name = "limit_amount", nullable = false)
    private BigDecimal limitAmount;

    //  bulan dalam format YYYY-MM, e.g. "2025-01"
    @Column(name = "budget_month", nullable = false, length = 7)
    private String budgetMonth;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // Helper — konversi String "YYYY-MM" ke YearMonth
    public YearMonth getYearMonth() {
        return YearMonth.parse(this.budgetMonth);
    }
}