package com.pennywise.expense;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserId(Long userId);
    List<Expense> findByUserIdAndBillingMonth(Long userId, String billingMonth);
    List<Expense> findByUserIdAndBudgetId(Long userId, Long budgetId);
    List<Expense> findByBillingMonth(String billingMonth);
    Optional<Expense> findByIdAndUserId(Long id, Long userId);
    boolean existsByIdAndUserId(Long id, Long userId);
}
