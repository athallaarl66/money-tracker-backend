package com.moneytracker.money_tracker_be.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private Long id;
    private String name;
    private String type;
    private BigDecimal initialBalance;
    private BigDecimal balance;
    private LocalDateTime createdAt;
}