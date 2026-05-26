package com.pennywise.notification;

import com.pennywise.budget.Budget;
import com.pennywise.budget.BudgetMonthlyArchive;
import com.pennywise.budget.BudgetMonthlyArchiveRepository;
import com.pennywise.budget.BudgetRepository;
import com.pennywise.expense.Expense;
import com.pennywise.expense.ExpenseMonthlyArchive;
import com.pennywise.expense.ExpenseMonthlyArchiveRepository;
import com.pennywise.expense.ExpenseRepository;
import java.util.ArrayList;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MonthlyDataArchiveScheduler {

    private static final Logger logger = LoggerFactory.getLogger(MonthlyDataArchiveScheduler.class);

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetMonthlyArchiveRepository budgetMonthlyArchiveRepository;
    private final ExpenseMonthlyArchiveRepository expenseMonthlyArchiveRepository;

    public MonthlyDataArchiveScheduler(
            BudgetRepository budgetRepository,
            ExpenseRepository expenseRepository,
            BudgetMonthlyArchiveRepository budgetMonthlyArchiveRepository,
            ExpenseMonthlyArchiveRepository expenseMonthlyArchiveRepository) {
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
        this.budgetMonthlyArchiveRepository = budgetMonthlyArchiveRepository;
        this.expenseMonthlyArchiveRepository = expenseMonthlyArchiveRepository;
    }

    @Scheduled(cron = "${pennywise.monthly-archive.cron:0 5 0 1 * ?}")
    @Transactional
    public void archivePreviousMonthData() {
        String currentMonth = YearMonth.now(ZoneOffset.UTC).toString();
        String monthToArchive = YearMonth.now(ZoneOffset.UTC).minusMonths(1).toString();
        logger.info("Starting monthly archive job for billing month {}", monthToArchive);

        List<Budget> budgets = budgetRepository.findByBillingMonth(monthToArchive);
        List<Expense> expenses = expenseRepository.findByBillingMonth(monthToArchive);

        if (budgets.isEmpty() && expenses.isEmpty()) {
            logger.info("No monthly data found for {}", monthToArchive);
            return;
        }

        if (!budgets.isEmpty()) {
            List<BudgetMonthlyArchive> archivedBudgets = new ArrayList<>();
            for (Budget budget : budgets) {
                archivedBudgets.add(toBudgetArchive(budget));
            }
            budgetMonthlyArchiveRepository.saveAll(archivedBudgets);
            budgetRepository.deleteAll(budgets);

            if (budgetRepository.findByBillingMonth(currentMonth).isEmpty()) {
                List<Budget> currentMonthBudgets = new ArrayList<>();
                for (Budget budget : budgets) {
                    currentMonthBudgets.add(cloneBudgetForMonth(budget, currentMonth));
                }
                budgetRepository.saveAll(currentMonthBudgets);
            }
        }

        if (!expenses.isEmpty()) {
            List<ExpenseMonthlyArchive> archivedExpenses = new ArrayList<>();
            for (Expense expense : expenses) {
                archivedExpenses.add(toExpenseArchive(expense));
            }
            expenseMonthlyArchiveRepository.saveAll(archivedExpenses);
            expenseRepository.deleteAll(expenses);
        }

        logger.info("Completed monthly archive job for {}. Archived {} budgets and {} expenses.", monthToArchive, budgets.size(), expenses.size());
    }

    private BudgetMonthlyArchive toBudgetArchive(Budget budget) {
        return new BudgetMonthlyArchive(
                budget.getId(),
                budget.getUserId(),
                budget.getName(),
                budget.getIcon(),
                budget.getAmount(),
                budget.getPeriod(),
                budget.getBillingMonth(),
                budget.getStartDate(),
                budget.getEndDate()
        );
    }

    private ExpenseMonthlyArchive toExpenseArchive(Expense expense) {
        return new ExpenseMonthlyArchive(
                expense.getId(),
                expense.getUserId(),
                expense.getBudgetId(),
                expense.getCategoryId(),
                expense.getTitle(),
                expense.getAmount(),
                expense.getExpenseDate(),
                expense.getBillingMonth(),
                expense.getNotes()
        );
    }

    private Budget cloneBudgetForMonth(Budget source, String billingMonth) {
        Budget budget = new Budget();
        budget.setUserId(source.getUserId());
        budget.setName(source.getName());
        budget.setIcon(source.getIcon());
        budget.setAmount(source.getAmount());
        budget.setPeriod(source.getPeriod());
        budget.setBillingMonth(billingMonth);
        budget.setStartDate(source.getStartDate());
        budget.setEndDate(source.getEndDate());
        return budget;
    }
}