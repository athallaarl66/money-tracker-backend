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
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    private User getUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Budget getBudgetOrThrow(String email, Long budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        if (!budget.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Budget not found or unauthorized");
        }
        return budget;
    }

    private BigDecimal calculateSpent(Long userId, String category, String budgetMonth) {
        YearMonth ym = YearMonth.parse(budgetMonth);
        LocalDate startOfMonth = ym.atDay(1);
        LocalDate endOfMonth = ym.atEndOfMonth();

        return transactionRepository.findByAccountUserId(userId)
                .stream()
                .filter(t -> t.getTransactionType() == TransactionType.EXPENSE)
                .filter(t -> t.getCategory() != null && t.getCategory().equalsIgnoreCase(category))
                .filter(t -> {
                    LocalDate date = t.getTransactionDate();
                    return date != null && !date.isBefore(startOfMonth) && !date.isAfter(endOfMonth);
                })
                .map(t -> t.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BudgetResponse toResponse(Budget budget, Long userId) {
        BigDecimal spent = calculateSpent(userId, budget.getCategory(), budget.getBudgetMonth());
        BigDecimal limit = budget.getLimitAmount();
        BigDecimal remaining = limit.subtract(spent);

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
                .percentage(Math.round(percentage * 10.0) / 10.0)
                .remainingAmount(remaining)
                .budgetMonth(budget.getBudgetMonth())
                .build();
    }

    public BudgetResponse create(String email, BudgetRequest request) {
        User user = getUserOrThrow(email);

        boolean alreadyExists = budgetRepository
                .findByUserIdAndCategoryAndBudgetMonth(
                        user.getId(), request.getCategory(), request.getBudgetMonth())
                .isPresent();

        if (alreadyExists) {
            throw new RuntimeException(
                    "Budget for category '" + request.getCategory() + "' already exists this month");
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

        boolean categoryOrMonthChanged =
                !budget.getCategory().equals(request.getCategory()) ||
                        !budget.getBudgetMonth().equals(request.getBudgetMonth());

        if (categoryOrMonthChanged) {
            boolean conflict = budgetRepository
                    .findByUserIdAndCategoryAndBudgetMonth(
                            user.getId(), request.getCategory(), request.getBudgetMonth())
                    .filter(existing -> !existing.getId().equals(budgetId))
                    .isPresent();

            if (conflict) {
                throw new RuntimeException(
                        "Budget for category '" + request.getCategory() + "' already exists this month");
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