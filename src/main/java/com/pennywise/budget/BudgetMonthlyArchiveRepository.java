package com.pennywise.budget;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BudgetMonthlyArchiveRepository extends JpaRepository<BudgetMonthlyArchive, Long> {
	List<BudgetMonthlyArchive> findByUserIdAndBillingMonth(Long userId, String billingMonth);
	List<BudgetMonthlyArchive> findByBillingMonth(String billingMonth);
}