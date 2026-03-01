package com.moneytracker.money_tracker_be.controller;

import com.moneytracker.money_tracker_be.dto.AccountRequest;
import com.moneytracker.money_tracker_be.dto.AccountResponse;
import com.moneytracker.money_tracker_be.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AccountRequest request) {
        return ResponseEntity.ok(accountService.create(userDetails.getUsername(), request));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAll(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(accountService.getAll(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(accountService.getById(userDetails.getUsername(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody AccountRequest request) {
        return ResponseEntity.ok(accountService.update(userDetails.getUsername(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        accountService.delete(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}