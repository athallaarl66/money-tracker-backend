package com.moneytracker.money_tracker_be.controller;

import com.moneytracker.money_tracker_be.dto.TransactionRequest;
import com.moneytracker.money_tracker_be.dto.TransactionResponse;
import com.moneytracker.money_tracker_be.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // ── Per-account endpoints ─────────────────────────────────────────────────

    @PostMapping("/api/accounts/{accountId}/transactions")
    public ResponseEntity<TransactionResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long accountId,
            @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(
                transactionService.create(userDetails.getUsername(), accountId, request));
    }

    @GetMapping("/api/accounts/{accountId}/transactions")
    public ResponseEntity<List<TransactionResponse>> getAllByAccount(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long accountId) {
        return ResponseEntity.ok(
                transactionService.getAll(userDetails.getUsername(), accountId));
    }

    @GetMapping("/api/accounts/{accountId}/transactions/{id}")
    public ResponseEntity<TransactionResponse> getById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long accountId,
            @PathVariable Long id) {
        return ResponseEntity.ok(
                transactionService.getById(userDetails.getUsername(), accountId, id));
    }

    @PutMapping("/api/accounts/{accountId}/transactions/{id}")
    public ResponseEntity<TransactionResponse> update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long accountId,
            @PathVariable Long id,
            @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(
                transactionService.update(userDetails.getUsername(), accountId, id, request));
    }

    @DeleteMapping("/api/accounts/{accountId}/transactions/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long accountId,
            @PathVariable Long id) {
        transactionService.delete(userDetails.getUsername(), accountId, id);
        return ResponseEntity.noContent().build();
    }

    // ── Get ALL transactions lintas semua account ─────────────────────────────

    @GetMapping("/api/transactions")
    public ResponseEntity<List<TransactionResponse>> getAllTransactions(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                transactionService.getAllByUser(userDetails.getUsername()));
    }

    // ── Export CSV ────────────────────────────────────────────────────────────

    /**
     * GET /api/transactions/export
     * Query params (opsional):
     *   ?year=2026&month=3  → export bulan tertentu
     *   (tanpa params)      → export semua transaksi
     *
     * Response: file .csv langsung ke-download di browser
     */
    @GetMapping("/api/transactions/export")
    public ResponseEntity<byte[]> exportCsv(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        String csv = transactionService.exportCsv(
                userDetails.getUsername(), year, month);

        // Nama file dinamis — kalau ada filter bulan, include di nama file
        String filename = buildFilename(year, month);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    // Format nama file: transactions-2026-03.csv atau transactions-all.csv
    private String buildFilename(Integer year, Integer month) {
        if (year != null && month != null) {
            return String.format("transactions-%d-%02d.csv", year, month);
        }
        return "transactions-all.csv";
    }
}