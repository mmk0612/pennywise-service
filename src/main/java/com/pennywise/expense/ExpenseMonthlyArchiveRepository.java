package com.pennywise.expense;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExpenseMonthlyArchiveRepository extends JpaRepository<ExpenseMonthlyArchive, Long> {
	List<ExpenseMonthlyArchive> findByUserIdAndBillingMonth(Long userId, String billingMonth);
	List<ExpenseMonthlyArchive> findByBillingMonth(String billingMonth);
}