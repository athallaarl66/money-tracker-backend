package com.moneytracker.money_tracker_be.repository;

import com.moneytracker.money_tracker_be.entity.Account;
import com.moneytracker.money_tracker_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    // existing
    List<Account> findByUser(User user);
    Optional<Account> findByIdAndUser(Long id, User user);

    // tambahan
    List<Account> findByUserId(Long userId);
    Optional<Account> findByIdAndUserId(Long id, Long userId);

}