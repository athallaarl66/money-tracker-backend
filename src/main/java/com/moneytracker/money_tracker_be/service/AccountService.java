package com.moneytracker.money_tracker_be.service;

import com.moneytracker.money_tracker_be.dto.AccountRequest;
import com.moneytracker.money_tracker_be.dto.AccountResponse;
import com.moneytracker.money_tracker_be.entity.Account;
import com.moneytracker.money_tracker_be.entity.User;
import com.moneytracker.money_tracker_be.repository.AccountRepository;
import com.moneytracker.money_tracker_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    // Helper: lookup user by email (username dari JWT = email)
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Helper: convert entity → DTO
    private AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .type(account.getType())
                .initialBalance(account.getInitialBalance())
                .balance(account.getBalance())
                .createdAt(account.getCreatedAt())
                .build();
    }

    // Nama method disesuaikan dengan yang dipanggil Controller
    public AccountResponse create(String email, AccountRequest request) {
        User user = getUserByEmail(email);

        BigDecimal initialBalance = request.getInitialBalance() != null
                ? request.getInitialBalance()
                : BigDecimal.ZERO;

        Account account = Account.builder()
                .user(user)
                .name(request.getName())
                .type(request.getType())
                .initialBalance(initialBalance)
                .balance(initialBalance)
                .build();

        return toResponse(accountRepository.save(account));
    }

    public List<AccountResponse> getAll(String email) {
        User user = getUserByEmail(email);
        return accountRepository.findByUserId(user.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AccountResponse getById(String email, Long accountId) {
        User user = getUserByEmail(email);
        Account account = accountRepository.findByIdAndUserId(accountId, user.getId())
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return toResponse(account);
    }

    public AccountResponse update(String email, Long accountId, AccountRequest request) {
        User user = getUserByEmail(email);
        Account account = accountRepository.findByIdAndUserId(accountId, user.getId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setName(request.getName());
        account.setType(request.getType());

        return toResponse(accountRepository.save(account));
    }

    public void delete(String email, Long accountId) {
        User user = getUserByEmail(email);
        Account account = accountRepository.findByIdAndUserId(accountId, user.getId())
                .orElseThrow(() -> new RuntimeException("Account not found"));
        accountRepository.delete(account);
    }
}