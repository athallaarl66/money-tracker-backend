package com.moneytracker.money_tracker_be.controller;

import com.moneytracker.money_tracker_be.dto.RecurringTransactionRequest;
import com.moneytracker.money_tracker_be.dto.RecurringTransactionResponse;
import com.moneytracker.money_tracker_be.service.RecurringTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recurring")
@RequiredArgsConstructor
public class RecurringTransactionController {

    private final RecurringTransactionService recurringService;

    // GET /api/recurring — semua recurring milik user
    @GetMapping
    public ResponseEntity<List<RecurringTransactionResponse>> getAll(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(recurringService.getAll(userDetails.getUsername()));
    }

    // POST /api/recurring — buat recurring baru
    @PostMapping
    public ResponseEntity<RecurringTransactionResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RecurringTransactionRequest request
    ) {
        return ResponseEntity.ok(recurringService.create(userDetails.getUsername(), request));
    }

    // PUT /api/recurring/{id} — update recurring
    @PutMapping("/{id}")
    public ResponseEntity<RecurringTransactionResponse> update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody RecurringTransactionRequest request
    ) {
        return ResponseEntity.ok(recurringService.update(userDetails.getUsername(), id, request));
    }

    // PATCH /api/recurring/{id}/toggle — pause/resume tanpa hapus
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<RecurringTransactionResponse> toggleActive(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(recurringService.toggleActive(userDetails.getUsername(), id));
    }

    // DELETE /api/recurring/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        recurringService.delete(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}