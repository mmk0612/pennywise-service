package com.pennywise.budget;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "budget_monthly_archives")
public class BudgetMonthlyArchive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sourceBudgetId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column
    private String icon;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String period;

    @Column(nullable = false)
    private String billingMonth;

    @Column
    private Instant startDate;

    @Column
    private Instant endDate;

    @Column(nullable = false, updatable = false)
    private Instant archivedAt = Instant.now();

    public BudgetMonthlyArchive() {
    }

    public BudgetMonthlyArchive(Long sourceBudgetId, Long userId, String name, String icon, BigDecimal amount, String period, String billingMonth, Instant startDate, Instant endDate) {
        this.sourceBudgetId = sourceBudgetId;
        this.userId = userId;
        this.name = name;
        this.icon = icon;
        this.amount = amount;
        this.period = period;
        this.billingMonth = billingMonth;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getId() { return id; }
    public Long getSourceBudgetId() { return sourceBudgetId; }
    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getIcon() { return icon; }
    public BigDecimal getAmount() { return amount; }
    public String getPeriod() { return period; }
    public String getBillingMonth() { return billingMonth; }
    public Instant getStartDate() { return startDate; }
    public Instant getEndDate() { return endDate; }
    public Instant getArchivedAt() { return archivedAt; }
}