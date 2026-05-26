package com.pennywise.expense;

import java.math.BigDecimal;
import java.time.Instant;

public class ExpenseDTO {

    public record CreateExpenseRequest(
            String title,
            BigDecimal amount,
            Instant expenseDate,
            String billingMonth,
            Long budgetId,
            Long categoryId,
            String notes
    ) {}

    public record UpdateExpenseRequest(
            String title,
            BigDecimal amount,
            Instant expenseDate,
            String billingMonth,
            Long budgetId,
            Long categoryId,
            String notes
    ) {}

    public record ExpenseResponse(
            Long id,
            Long userId,
            String title,
            BigDecimal amount,
            Instant expenseDate,
            String billingMonth,
            Long budgetId,
            Long categoryId,
            String notes,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
