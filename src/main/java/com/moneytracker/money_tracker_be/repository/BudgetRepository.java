package com.moneytracker.money_tracker_be.repository;

import com.moneytracker.money_tracker_be.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    // Semua budget milik user di bulan tertentu — buat halaman budget
    List<Budget> findByUserIdAndBudgetMonth(Long userId, String budgetMonth);

    // Cek duplikat sebelum create — satu kategori per bulan per user
    Optional<Budget> findByUserIdAndCategoryAndBudgetMonth(Long userId, String category, String budgetMonth);

    // Semua budget milik user — fallback kalau butuh semua data
    List<Budget> findByUserId(Long userId);
}