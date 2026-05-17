package com.pennywise.dashboard;

import com.pennywise.budget.BudgetRepository;
import com.pennywise.expense.ExpenseRepository;
import com.pennywise.budget.Budget;
import com.pennywise.expense.Expense;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {
    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    public DashboardService(BudgetRepository budgetRepository, ExpenseRepository expenseRepository) {
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
    }

    public DashboardDTO.DashboardResponse getDashboardData(Long userId) {
        List<Budget> userBudgets = budgetRepository.findByUserId(userId);
        List<Expense> userExpenses = expenseRepository.findByUserId(userId);

        DashboardDTO.DashboardSummary summary = computeSummary(userBudgets, userExpenses);
        List<DashboardDTO.ChartDataPoint> expensesByBudget = computeExpensesByBudget(userBudgets, userExpenses);
        List<DashboardDTO.ChartDataPoint> expensesByCategory = computeExpensesByCategory(userExpenses);

        return new DashboardDTO.DashboardResponse(summary, expensesByBudget, expensesByCategory);
    }

    private DashboardDTO.DashboardSummary computeSummary(List<Budget> budgets, List<Expense> expenses) {
        BigDecimal totalBudgets = budgets.stream()
                .map(Budget::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingBudget = totalBudgets.subtract(totalExpenses);

        return new DashboardDTO.DashboardSummary(
                totalBudgets,
                totalExpenses,
                remainingBudget,
                (long) budgets.size(),
                (long) expenses.size()
        );
    }

    private List<DashboardDTO.ChartDataPoint> computeExpensesByBudget(List<Budget> budgets, List<Expense> expenses) {
        Map<Long, BigDecimal> expensesByBudgetId = expenses.stream()
                .filter(e -> e.getBudgetId() != null)
                .collect(Collectors.groupingBy(
                        Expense::getBudgetId,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        return budgets.stream()
                .map(budget -> new DashboardDTO.ChartDataPoint(
                        budget.getName(),
                        expensesByBudgetId.getOrDefault(budget.getId(), BigDecimal.ZERO)
                ))
                .collect(Collectors.toList());
    }

    private List<DashboardDTO.ChartDataPoint> computeExpensesByCategory(List<Expense> expenses) {
        Map<String, BigDecimal> expensesByCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategoryId() != null ? "Category_" + e.getCategoryId() : "Uncategorized",
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        return expensesByCategory.entrySet().stream()
                .map(entry -> new DashboardDTO.ChartDataPoint(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
