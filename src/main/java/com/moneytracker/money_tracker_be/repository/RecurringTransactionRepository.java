package com.moneytracker.money_tracker_be.repository;

import com.moneytracker.money_tracker_be.entity.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {

    // Semua recurring milik user — buat halaman list
    List<RecurringTransaction> findByUserId(Long userId);

    // Yang aktif dan sudah waktunya dijalankan — dipake scheduler
    List<RecurringTransaction> findByActiveTrueAndNextRunDateLessThanEqual(LocalDate date);
}