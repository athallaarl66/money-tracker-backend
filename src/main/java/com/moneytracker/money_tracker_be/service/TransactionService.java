package com.moneytracker.money_tracker_be.service;

import com.moneytracker.money_tracker_be.dto.TransactionRequest;
import com.moneytracker.money_tracker_be.dto.TransactionResponse;
import com.moneytracker.money_tracker_be.entity.Account;
import com.moneytracker.money_tracker_be.entity.Transaction;
import com.moneytracker.money_tracker_be.entity.Transaction.TransactionType;
import com.moneytracker.money_tracker_be.repository.AccountRepository;
import com.moneytracker.money_tracker_be.repository.TransactionRepository;
import com.moneytracker.money_tracker_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    private Account getAccountOrThrow(String email, Long accountId) {
        return accountRepository.findById(accountId)
                .filter(a -> a.getUser().getEmail().equals(email))
                .orElseThrow(() -> new RuntimeException("Account not found or unauthorized"));
    }

    // Pakai .builder() karena TransactionResponse pakai @Builder (bukan @NoArgsConstructor)
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

    // Tambah efek ke balance (income = add, expense = subtract)
    private void applyBalance(Account account, TransactionType type, BigDecimal amount) {
        if (type == TransactionType.INCOME) {
            account.setBalance(account.getBalance().add(amount));
        } else {
            account.setBalance(account.getBalance().subtract(amount));
        }
    }

    // Balik efek ke balance (kebalikan dari applyBalance)
    private void reverseBalance(Account account, TransactionType type, BigDecimal amount) {
        if (type == TransactionType.INCOME) {
            account.setBalance(account.getBalance().subtract(amount));
        } else {
            account.setBalance(account.getBalance().add(amount));
        }
    }

    // CREATE
    public TransactionResponse create(String email, Long accountId, TransactionRequest request) {
        Account account = getAccountOrThrow(email, accountId);

        Transaction transaction = Transaction.builder()
                .account(account)
                .description(request.getDescription())
                .amount(request.getAmount())
                .transactionType(request.getTransactionType())
                .category(request.getCategory())
                .transactionDate(request.getTransactionDate())
                .build();

        applyBalance(account, request.getTransactionType(), request.getAmount());
        accountRepository.save(account);
        return toResponse(transactionRepository.save(transaction));
    }

    // GET ALL per account
    public List<TransactionResponse> getAll(String email, Long accountId) {
        getAccountOrThrow(email, accountId);
        return transactionRepository.findByAccountId(accountId)
                .stream().map(this::toResponse).toList();
    }

    // GET ALL lintas semua account milik user — endpoint baru GET /api/transactions
    public List<TransactionResponse> getAllByUser(String email) {
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        return transactionRepository.findByAccountUserId(userId)
                .stream()
                .sorted((a, b) -> b.getTransactionDate().compareTo(a.getTransactionDate()))
                .map(this::toResponse)
                .toList();
    }

    // GET BY ID
    public TransactionResponse getById(String email, Long accountId, Long id) {
        getAccountOrThrow(email, accountId);
        Transaction t = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        return toResponse(t);
    }

    // UPDATE
    public TransactionResponse update(String email, Long accountId, Long id, TransactionRequest request) {
        Account account = getAccountOrThrow(email, accountId);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        reverseBalance(account, transaction.getTransactionType(), transaction.getAmount());
        applyBalance(account, request.getTransactionType(), request.getAmount());

        transaction.setDescription(request.getDescription());
        transaction.setAmount(request.getAmount());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setCategory(request.getCategory());
        transaction.setTransactionDate(request.getTransactionDate());

        accountRepository.save(account);
        return toResponse(transactionRepository.save(transaction));
    }

    // DELETE
    public void delete(String email, Long accountId, Long id) {
        Account account = getAccountOrThrow(email, accountId);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        reverseBalance(account, transaction.getTransactionType(), transaction.getAmount());
        accountRepository.save(account);
        transactionRepository.delete(transaction);
    }
}