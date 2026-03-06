package com.moneytracker.money_tracker_be.service;

import com.moneytracker.money_tracker_be.dto.BudgetRequest;
import com.moneytracker.money_tracker_be.dto.BudgetResponse;
import com.moneytracker.money_tracker_be.entity.Budget;
import com.moneytracker.money_tracker_be.entity.Transaction.TransactionType;
import com.moneytracker.money_tracker_be.entity.User;
import com.moneytracker.money_tracker_be.repository.BudgetRepository;
import com.moneytracker.money_tracker_be.repository.TransactionRepository;
import com.moneytracker.money_tracker_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    // ─── Private helpers ──────────────────────────────────────────────────────

    private User getUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Budget getBudgetOrThrow(String email, Long budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        // Pastikan budget ini milik user yang request — cegah akses data orang lain
        if (!budget.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Budget not found or unauthorized");
        }
        return budget;
    }

    /**
     * Hitung total expense user di kategori tertentu dalam bulan tertentu.
     * Ini yang bikin progress bar akurat di FE.
     */
    private BigDecimal calculateSpent(Long userId, String category, String budgetMonth) {
        YearMonth ym = YearMonth.parse(budgetMonth);

        // Rentang tanggal awal dan akhir bulan
        LocalDateTime startOfMonth = ym.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = ym.atEndOfMonth().atTime(23, 59, 59);

        return transactionRepository.findByAccountUserId(userId)
                .stream()
                .filter(t -> t.getTransactionType() == TransactionType.EXPENSE)
                .filter(t -> t.getCategory() != null && t.getCategory().equalsIgnoreCase(category))
                .filter(t -> {
                    LocalDateTime date = t.getTransactionDate();
                    return !date.isBefore(startOfMonth) && !date.isAfter(endOfMonth);
                })
                .map(t -> t.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BudgetResponse toResponse(Budget budget, Long userId) {
        BigDecimal spent = calculateSpent(userId, budget.getCategory(), budget.getBudgetMonth());
        BigDecimal limit = budget.getLimitAmount();
        BigDecimal remaining = limit.subtract(spent);

        // Hitung persentase — pakai scale 2 biar presisi
        double percentage = 0.0;
        if (limit.compareTo(BigDecimal.ZERO) > 0) {
            percentage = spent.divide(limit, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        return BudgetResponse.builder()
                .id(budget.getId())
                .category(budget.getCategory())
                .limitAmount(limit)
                .spentAmount(spent)
                .percentage(Math.round(percentage * 10.0) / 10.0) // 1 desimal cukup
                .remainingAmount(remaining)
                .budgetMonth(budget.getBudgetMonth())
                .build();
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    public BudgetResponse create(String email, BudgetRequest request) {
        User user = getUserOrThrow(email);

        // Cek duplikat — satu kategori per bulan per user
        boolean alreadyExists = budgetRepository
                .findByUserIdAndCategoryAndBudgetMonth(
                        user.getId(),
                        request.getCategory(),
                        request.getBudgetMonth()
                ).isPresent();

        if (alreadyExists) {
            throw new RuntimeException(
                    "Budget untuk kategori '" + request.getCategory() + "' di bulan ini sudah ada"
            );
        }

        Budget budget = Budget.builder()
                .user(user)
                .category(request.getCategory())
                .limitAmount(request.getLimitAmount())
                .budgetMonth(request.getBudgetMonth())
                .build();

        return toResponse(budgetRepository.save(budget), user.getId());
    }

    public List<BudgetResponse> getByMonth(String email, String budgetMonth) {
        User user = getUserOrThrow(email);
        return budgetRepository
                .findByUserIdAndBudgetMonth(user.getId(), budgetMonth)
                .stream()
                .map(b -> toResponse(b, user.getId()))
                .toList();
    }

    public BudgetResponse update(String email, Long budgetId, BudgetRequest request) {
        User user = getUserOrThrow(email);
        Budget budget = getBudgetOrThrow(email, budgetId);

        // Kalau ganti kategori atau bulan, cek duplikat lagi
        boolean categoryOrMonthChanged =
                !budget.getCategory().equals(request.getCategory()) ||
                        !budget.getBudgetMonth().equals(request.getBudgetMonth());

        if (categoryOrMonthChanged) {
            boolean conflict = budgetRepository
                    .findByUserIdAndCategoryAndBudgetMonth(
                            user.getId(),
                            request.getCategory(),
                            request.getBudgetMonth()
                    )
                    .filter(existing -> !existing.getId().equals(budgetId))
                    .isPresent();

            if (conflict) {
                throw new RuntimeException(
                        "Budget untuk kategori '" + request.getCategory() + "' di bulan ini sudah ada"
                );
            }
        }

        budget.setCategory(request.getCategory());
        budget.setLimitAmount(request.getLimitAmount());
        budget.setBudgetMonth(request.getBudgetMonth());

        return toResponse(budgetRepository.save(budget), user.getId());
    }

    public void delete(String email, Long budgetId) {
        getBudgetOrThrow(email, budgetId);
        budgetRepository.deleteById(budgetId);
    }
}