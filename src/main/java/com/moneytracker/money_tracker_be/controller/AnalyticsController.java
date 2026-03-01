package com.moneytracker.money_tracker_be.controller;

import com.moneytracker.money_tracker_be.dto.*;
import com.moneytracker.money_tracker_be.repository.UserRepository;
import com.moneytracker.money_tracker_be.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserRepository userRepository;

    private Long getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    @GetMapping("/summary")
    public ResponseEntity<SummaryResponse> getSummary(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(
                analyticsService.getSummary(getUserId(userDetails), year, month));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategorySummaryResponse>> getCategoryBreakdown(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(
                analyticsService.getCategoryBreakdown(getUserId(userDetails), year, month));
    }

    @GetMapping("/monthly-trend")
    public ResponseEntity<List<MonthlyTrendResponse>> getMonthlyTrend(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(
                analyticsService.getMonthlyTrend(getUserId(userDetails), months));
    }

    @GetMapping("/account-balances")
    public ResponseEntity<List<AccountBalanceResponse>> getAccountBalances(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                analyticsService.getAccountBalances(getUserId(userDetails)));
    }
}