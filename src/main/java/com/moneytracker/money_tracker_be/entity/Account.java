package com.moneytracker.money_tracker_be.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type; // "CASH", "BANK", "E-WALLET"

    @Column(name = "initial_balance")
    private BigDecimal initialBalance;

    private BigDecimal balance;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.initialBalance == null) this.initialBalance = BigDecimal.ZERO;
        // balance selalu mulai dari initialBalance
        if (this.balance == null) this.balance = this.initialBalance;
    }
}