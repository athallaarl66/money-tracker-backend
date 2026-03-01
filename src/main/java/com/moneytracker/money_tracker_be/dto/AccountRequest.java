package com.moneytracker.money_tracker_be.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {
    private String name;
    private String type;
    private BigDecimal initialBalance;
}