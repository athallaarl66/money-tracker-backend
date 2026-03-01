package com.moneytracker.money_tracker_be.controller;

import com.moneytracker.money_tracker_be.dto.AuthResponse;
import com.moneytracker.money_tracker_be.dto.LoginRequest;
import com.moneytracker.money_tracker_be.dto.RegisterRequest;
import com.moneytracker.money_tracker_be.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}