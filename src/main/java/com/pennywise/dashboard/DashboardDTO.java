package com.pennywise.dashboard;

import java.math.BigDecimal;
import java.util.List;

public class DashboardDTO {

    public record SummaryCard(
            String title,
            BigDecimal value,
            String description
    ) {}

    public record ChartDataPoint(
            String label,
            BigDecimal value
    ) {}

    public record DashboardSummary(
            BigDecimal totalBudgets,
            BigDecimal totalExpenses,
            BigDecimal remainingBudget,
            Long totalBudgetCount,
            Long totalExpenseCount
    ) {}

    public record DashboardChartData(
            String type,
            List<ChartDataPoint> data
    ) {}

    public record DashboardResponse(
            DashboardSummary summary,
            List<ChartDataPoint> expensesByBudget,
            List<ChartDataPoint> expensesByCategory
    ) {}
}
