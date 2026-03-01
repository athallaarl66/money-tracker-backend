package com.moneytracker.money_tracker_be.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}