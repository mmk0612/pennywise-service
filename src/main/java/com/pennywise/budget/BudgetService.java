package com.pennywise.budget;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final BudgetMonthlyArchiveRepository budgetMonthlyArchiveRepository;

    public BudgetService(BudgetRepository budgetRepository, BudgetMonthlyArchiveRepository budgetMonthlyArchiveRepository) {
        this.budgetRepository = budgetRepository;
        this.budgetMonthlyArchiveRepository = budgetMonthlyArchiveRepository;
    }

    public BudgetDTO.BudgetResponse createBudget(Long userId, BudgetDTO.CreateBudgetRequest request) {
        Budget budget = new Budget();
        budget.setUserId(userId);
        budget.setName(request.name());
        budget.setIcon(request.icon());
        budget.setAmount(request.amount());
        budget.setPeriod(request.period());
        budget.setBillingMonth(resolveBillingMonth(request.billingMonth(), request.startDate()));
        budget.setStartDate(request.startDate());
        budget.setEndDate(request.endDate());

        budget = budgetRepository.save(budget);
        return toBudgetResponse(budget);
    }

    public BudgetDTO.BudgetResponse getBudgetById(Long id, Long userId) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found"));
        return toBudgetResponse(budget);
    }

    public List<BudgetDTO.BudgetResponse> getUserBudgets(Long userId, String billingMonth) {
        String targetMonth = normalizeBillingMonth(billingMonth);
        List<Budget> budgets = targetMonth.equals(currentMonthKey())
                ? budgetRepository.findByUserIdAndBillingMonth(userId, targetMonth)
                : budgetMonthlyArchiveRepository.findByUserIdAndBillingMonth(userId, targetMonth).stream()
                        .map(this::toBudgetEntity)
                        .collect(Collectors.toList());

        if (budgets.isEmpty() && targetMonth.equals(currentMonthKey())) {
            budgets = budgetMonthlyArchiveRepository.findByUserIdAndBillingMonth(userId, previousMonthKey()).stream()
                    .map(this::toBudgetEntity)
                    .collect(Collectors.toList());
        }

        return budgets.stream()
                .map(this::toBudgetResponse)
                .collect(Collectors.toList());
    }

    public BudgetDTO.BudgetResponse updateBudget(Long id, Long userId, BudgetDTO.UpdateBudgetRequest request) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new IllegalArgumentException("Budget not found"));

        budget.setName(request.name());
        budget.setIcon(request.icon());
        budget.setAmount(request.amount());
        budget.setPeriod(request.period());
        budget.setBillingMonth(resolveBillingMonth(request.billingMonth(), request.startDate()));
        budget.setStartDate(request.startDate());
        budget.setEndDate(request.endDate());
        budget.setUpdatedAt(java.time.Instant.now());

        budget = budgetRepository.save(budget);
        return toBudgetResponse(budget);
    }

    public void deleteBudget(Long id, Long userId) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found"));
        budgetRepository.deleteById(Objects.requireNonNull(budget.getId()));
    }

    private BudgetDTO.BudgetResponse toBudgetResponse(Budget budget) {
        return new BudgetDTO.BudgetResponse(
                budget.getId(),
                budget.getUserId(),
                budget.getName(),
                budget.getIcon(),
                budget.getAmount(),
                budget.getPeriod(),
                budget.getBillingMonth(),
                budget.getStartDate(),
                budget.getEndDate(),
                budget.getCreatedAt(),
                budget.getUpdatedAt()
        );
    }

    private String resolveBillingMonth(String explicitBillingMonth, Instant referenceDate) {
        if (explicitBillingMonth != null && !explicitBillingMonth.isBlank()) {
            return explicitBillingMonth;
        }

        Instant effectiveDate = referenceDate != null ? referenceDate : Instant.now();
        return YearMonth.from(effectiveDate.atZone(ZoneOffset.UTC)).toString();
    }

    private String normalizeBillingMonth(String billingMonth) {
        return billingMonth != null && !billingMonth.isBlank() ? billingMonth : currentMonthKey();
    }

    private String currentMonthKey() {
        return YearMonth.now(ZoneOffset.UTC).toString();
    }

    private String previousMonthKey() {
        return YearMonth.now(ZoneOffset.UTC).minusMonths(1).toString();
    }

    private Budget toBudgetEntity(BudgetMonthlyArchive archive) {
        Budget budget = new Budget();
        budget.setId(archive.getId());
        budget.setUserId(archive.getUserId());
        budget.setName(archive.getName());
        budget.setIcon(archive.getIcon());
        budget.setAmount(archive.getAmount());
        budget.setPeriod(archive.getPeriod());
        budget.setBillingMonth(archive.getBillingMonth());
        budget.setStartDate(archive.getStartDate());
        budget.setEndDate(archive.getEndDate());
        return budget;
    }
}
