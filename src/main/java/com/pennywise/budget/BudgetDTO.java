package com.pennywise.budget;

import java.math.BigDecimal;
import java.time.Instant;

public class BudgetDTO {

    public record CreateBudgetRequest(
            String name,
            BigDecimal amount,
            String period,
            Instant startDate,
            Instant endDate
    ) {}

    public record UpdateBudgetRequest(
            String name,
            BigDecimal amount,
            String period,
            Instant startDate,
            Instant endDate
    ) {}

    public record BudgetResponse(
            Long id,
            Long userId,
            String name,
            BigDecimal amount,
            String period,
            Instant startDate,
            Instant endDate,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
