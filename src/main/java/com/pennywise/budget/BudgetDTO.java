package com.pennywise.budget;

import java.math.BigDecimal;
import java.time.Instant;

public class BudgetDTO {

    public record CreateBudgetRequest(
            String name,
            String icon,
            BigDecimal amount,
            String period,
            String billingMonth,
            Instant startDate,
            Instant endDate
    ) {}

    public record UpdateBudgetRequest(
            String name,
            String icon,
            BigDecimal amount,
            String period,
            String billingMonth,
            Instant startDate,
            Instant endDate
    ) {}

    public record BudgetResponse(
            Long id,
            Long userId,
            String name,
            String icon,
            BigDecimal amount,
            String period,
            String billingMonth,
            Instant startDate,
            Instant endDate,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
