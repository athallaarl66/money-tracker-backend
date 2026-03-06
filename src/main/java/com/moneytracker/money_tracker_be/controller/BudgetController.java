package com.moneytracker.money_tracker_be.controller;

import com.moneytracker.money_tracker_be.dto.BudgetRequest;
import com.moneytracker.money_tracker_be.dto.BudgetResponse;
import com.moneytracker.money_tracker_be.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    /**
     * GET /api/budgets?month=2025-01
     * Ambil semua budget user di bulan tertentu.
     * Default ke bulan sekarang kalau param ga dikasih.
     */
    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getByMonth(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String month
    ) {
        // Fallback ke bulan ini kalau month ga di-provide
        String targetMonth = (month != null && !month.isBlank())
                ? month
                : YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        List<BudgetResponse> budgets = budgetService.getByMonth(userDetails.getUsername(), targetMonth);
        return ResponseEntity.ok(budgets);
    }

    /**
     * POST /api/budgets
     * Bikin budget baru untuk kategori + bulan tertentu.
     */
    @PostMapping
    public ResponseEntity<BudgetResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BudgetRequest request
    ) {
        BudgetResponse created = budgetService.create(userDetails.getUsername(), request);
        return ResponseEntity.ok(created);
    }

    /**
     * PUT /api/budgets/{id}
     * Update limit atau kategori budget yang sudah ada.
     */
    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request
    ) {
        BudgetResponse updated = budgetService.update(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/budgets/{id}
     * Hapus budget — cuma bisa hapus punya sendiri.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        budgetService.delete(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}