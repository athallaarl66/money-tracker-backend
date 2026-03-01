package com.moneytracker.money_tracker_be.controller;

import com.moneytracker.money_tracker_be.dto.TransactionRequest;
import com.moneytracker.money_tracker_be.dto.TransactionResponse;
import com.moneytracker.money_tracker_be.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts/{accountId}/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long accountId,
            @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.create(userDetails.getUsername(), accountId, request));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAll(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long accountId) {
        return ResponseEntity.ok(transactionService.getAll(userDetails.getUsername(), accountId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long accountId,
            @PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getById(userDetails.getUsername(), accountId, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long accountId,
            @PathVariable Long id,
            @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.update(userDetails.getUsername(), accountId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long accountId,
            @PathVariable Long id) {
        transactionService.delete(userDetails.getUsername(), accountId, id);
        return ResponseEntity.noContent().build();
    }
}
