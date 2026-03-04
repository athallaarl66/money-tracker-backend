package com.moneytracker.money_tracker_be.service;

import com.moneytracker.money_tracker_be.dto.TransactionRequest;
import com.moneytracker.money_tracker_be.dto.TransactionResponse;
import com.moneytracker.money_tracker_be.entity.Account;
import com.moneytracker.money_tracker_be.entity.Transaction;
import com.moneytracker.money_tracker_be.repository.AccountRepository;
import com.moneytracker.money_tracker_be.repository.TransactionRepository;
import com.moneytracker.money_tracker_be.repository.UserRepository;
import com.moneytracker.money_tracker_be.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    // Validasi account milik user yang login
    private Account getAccount(String username, Long accountId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return accountRepository.findByIdAndUser(accountId, user)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    private TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .accountId(t.getAccount().getId())
                .accountName(t.getAccount().getName())
                .description(t.getDescription())
                .amount(t.getAmount())
                .transactionType(t.getTransactionType())
                .category(t.getCategory())
                .transactionDate(t.getTransactionDate())
                .createdAt(t.getCreatedAt())
                .build();
    }

    public TransactionResponse create(String username, Long accountId, TransactionRequest request) {
        Account account = getAccount(username, accountId);

        Transaction transaction = Transaction.builder()
                .account(account)
                .description(request.getDescription())
                .amount(request.getAmount())
                .transactionType(request.getTransactionType())
                .category(request.getCategory())
                .transactionDate(request.getTransactionDate())
                .build();

        transactionRepository.save(transaction);

        // INCOME → balance naik, EXPENSE → balance turun
        if (request.getTransactionType().equals("INCOME")) {
            account.setBalance(account.getBalance().add(request.getAmount()));
        } else {
            account.setBalance(account.getBalance().subtract(request.getAmount()));
        }
        accountRepository.save(account);

        return toResponse(transaction);
    }

    public List<TransactionResponse> getAll(String username, Long accountId) {
        Account account = getAccount(username, accountId);
        return transactionRepository.findByAccount(account)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TransactionResponse getById(String username, Long accountId, Long id) {
        Account account = getAccount(username, accountId);
        Transaction transaction = transactionRepository.findByIdAndAccount(id, account)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        return toResponse(transaction);
    }

    public TransactionResponse update(String username, Long accountId, Long id, TransactionRequest request) {
        Account account = getAccount(username, accountId);
        Transaction transaction = transactionRepository.findByIdAndAccount(id, account)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Balik dulu efek transaksi lama ke balance
        if (transaction.getTransactionType().equals("INCOME")) {
            account.setBalance(account.getBalance().subtract(transaction.getAmount()));
        } else {
            account.setBalance(account.getBalance().add(transaction.getAmount()));
        }

        // Terapkan efek transaksi baru ke balance
        if (request.getTransactionType().equals("INCOME")) {
            account.setBalance(account.getBalance().add(request.getAmount()));
        } else {
            account.setBalance(account.getBalance().subtract(request.getAmount()));
        }

        transaction.setDescription(request.getDescription());
        transaction.setAmount(request.getAmount());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setCategory(request.getCategory());
        if (request.getTransactionDate() != null) {
            transaction.setTransactionDate(request.getTransactionDate());
        }

        accountRepository.save(account);
        return toResponse(transactionRepository.save(transaction));
    }

    public void delete(String username, Long accountId, Long id) {
        Account account = getAccount(username, accountId);
        Transaction transaction = transactionRepository.findByIdAndAccount(id, account)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Kembalikan balance sebelum hapus
        // Kebalikan dari create: INCOME dihapus → balance turun, EXPENSE dihapus → balance naik
        if (transaction.getTransactionType().equals("INCOME")) {
            account.setBalance(account.getBalance().subtract(transaction.getAmount()));
        } else {
            account.setBalance(account.getBalance().add(transaction.getAmount()));
        }
        accountRepository.save(account);

        transactionRepository.delete(transaction);
    }
}