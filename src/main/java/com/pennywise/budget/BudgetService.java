package com.pennywise.budget;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BudgetService {
    private static final Logger log = LoggerFactory.getLogger(BudgetService.class);

    private final BudgetRepository budgetRepository;

    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    public BudgetDTO.BudgetResponse createBudget(Long userId, BudgetDTO.CreateBudgetRequest request) {
        Budget budget = new Budget();
        budget.setUserId(userId);
        budget.setName(request.name());
        budget.setAmount(request.amount());
        budget.setPeriod(request.period());
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

    public List<BudgetDTO.BudgetResponse> getUserBudgets(Long userId) {
        return budgetRepository.findByUserId(userId)
                .stream()
                .map(this::toBudgetResponse)
                .collect(Collectors.toList());
    }

    public BudgetDTO.BudgetResponse updateBudget(Long id, Long userId, BudgetDTO.UpdateBudgetRequest request) {
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new IllegalArgumentException("Budget not found"));

        budget.setName(request.name());
        budget.setAmount(request.amount());
        budget.setPeriod(request.period());
        budget.setStartDate(request.startDate());
        budget.setEndDate(request.endDate());
        budget.setUpdatedAt(java.time.Instant.now());

        budget = budgetRepository.save(budget);
        return toBudgetResponse(budget);
    }

    public void deleteBudget(Long id, Long userId) {
        if (!budgetRepository.existsByIdAndUserId(id, userId)) {
            throw new IllegalArgumentException("Budget not found");
        }
        budgetRepository.deleteById(id);
    }

    private BudgetDTO.BudgetResponse toBudgetResponse(Budget budget) {
        return new BudgetDTO.BudgetResponse(
                budget.getId(),
                budget.getUserId(),
                budget.getName(),
                budget.getAmount(),
                budget.getPeriod(),
                budget.getStartDate(),
                budget.getEndDate(),
                budget.getCreatedAt(),
                budget.getUpdatedAt()
        );
    }
}
