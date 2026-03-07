package com.moneytracker.money_tracker_be.service;

import com.moneytracker.money_tracker_be.dto.RecurringTransactionRequest;
import com.moneytracker.money_tracker_be.dto.RecurringTransactionResponse;
import com.moneytracker.money_tracker_be.entity.Account;
import com.moneytracker.money_tracker_be.entity.RecurringTransaction;
import com.moneytracker.money_tracker_be.entity.RecurringTransaction.Frequency;
import com.moneytracker.money_tracker_be.entity.Transaction;
import com.moneytracker.money_tracker_be.entity.Transaction.TransactionType;
import com.moneytracker.money_tracker_be.entity.User;
import com.moneytracker.money_tracker_be.repository.AccountRepository;
import com.moneytracker.money_tracker_be.repository.RecurringTransactionRepository;
import com.moneytracker.money_tracker_be.repository.TransactionRepository;
import com.moneytracker.money_tracker_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecurringTransactionService {

    private final RecurringTransactionRepository recurringRepo;
    private final TransactionRepository transactionRepo;
    private final AccountRepository accountRepo;
    private final UserRepository userRepo;

    private User getUserOrThrow(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private RecurringTransaction getRecurringOrThrow(String email, Long id) {
        RecurringTransaction recurring = recurringRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Recurring transaction not found"));
        if (!recurring.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Recurring transaction not found or unauthorized");
        }
        return recurring;
    }

    private Account getAccountOrThrow(String email, Long accountId) {
        return accountRepo.findById(accountId)
                .filter(a -> a.getUser().getEmail().equals(email))
                .orElseThrow(() -> new RuntimeException("Account not found or unauthorized"));
    }

    private RecurringTransactionResponse toResponse(RecurringTransaction r) {
        return RecurringTransactionResponse.builder()
                .id(r.getId())
                .accountId(r.getAccount().getId())
                .accountName(r.getAccount().getName())
                .description(r.getDescription())
                .amount(r.getAmount())
                .transactionType(r.getTransactionType())
                .category(r.getCategory())
                .frequency(r.getFrequency())
                .nextRunDate(r.getNextRunDate())
                .active(r.isActive())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private LocalDate calculateNextRunDate(LocalDate from, Frequency frequency) {
        return switch (frequency) {
            case DAILY -> from.plusDays(1);
            case WEEKLY -> from.plusWeeks(1);
            case MONTHLY -> from.plusMonths(1);
            case YEARLY -> from.plusYears(1);
        };
    }

    @Transactional
    private void executeRecurring(RecurringTransaction recurring) {
        Account account = recurring.getAccount();

        Transaction transaction = Transaction.builder()
                .account(account)
                .description(recurring.getDescription())
                .amount(recurring.getAmount())
                .transactionType(recurring.getTransactionType())
                .category(recurring.getCategory())
                .transactionDate(LocalDate.now())
                .build();

        if (recurring.getTransactionType() == TransactionType.INCOME) {
            account.setBalance(account.getBalance().add(recurring.getAmount()));
        } else {
            account.setBalance(account.getBalance().subtract(recurring.getAmount()));
        }

        accountRepo.save(account);
        transactionRepo.save(transaction);

        recurring.setNextRunDate(calculateNextRunDate(recurring.getNextRunDate(), recurring.getFrequency()));
        recurringRepo.save(recurring);

        log.info("Recurring executed: {} - {} {}", recurring.getDescription(),
                recurring.getTransactionType(), recurring.getAmount());
    }

    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void processRecurringTransactions() {
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> dueList = recurringRepo
                .findByActiveTrueAndNextRunDateLessThanEqual(today);

        if (dueList.isEmpty()) return;

        log.info("Processing {} recurring transactions for {}", dueList.size(), today);

        for (RecurringTransaction recurring : dueList) {
            try {
                executeRecurring(recurring);
            } catch (Exception e) {
                log.error("Failed to execute recurring {}: {}", recurring.getId(), e.getMessage());
            }
        }
    }

    public RecurringTransactionResponse create(String email, RecurringTransactionRequest request) {
        User user = getUserOrThrow(email);
        Account account = getAccountOrThrow(email, request.getAccountId());

        RecurringTransaction recurring = RecurringTransaction.builder()
                .user(user)
                .account(account)
                .description(request.getDescription())
                .amount(request.getAmount())
                .transactionType(request.getTransactionType())
                .category(request.getCategory())
                .frequency(request.getFrequency())
                .nextRunDate(request.getStartDate())
                .active(true)
                .build();

        return toResponse(recurringRepo.save(recurring));
    }

    public List<RecurringTransactionResponse> getAll(String email) {
        User user = getUserOrThrow(email);
        return recurringRepo.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public RecurringTransactionResponse update(String email, Long id, RecurringTransactionRequest request) {
        RecurringTransaction recurring = getRecurringOrThrow(email, id);
        Account account = getAccountOrThrow(email, request.getAccountId());

        recurring.setAccount(account);
        recurring.setDescription(request.getDescription());
        recurring.setAmount(request.getAmount());
        recurring.setTransactionType(request.getTransactionType());
        recurring.setCategory(request.getCategory());
        recurring.setFrequency(request.getFrequency());
        recurring.setNextRunDate(request.getStartDate());

        return toResponse(recurringRepo.save(recurring));
    }

    public RecurringTransactionResponse toggleActive(String email, Long id) {
        RecurringTransaction recurring = getRecurringOrThrow(email, id);
        recurring.setActive(!recurring.isActive());
        return toResponse(recurringRepo.save(recurring));
    }

    public void delete(String email, Long id) {
        getRecurringOrThrow(email, id);
        recurringRepo.deleteById(id);
    }
}