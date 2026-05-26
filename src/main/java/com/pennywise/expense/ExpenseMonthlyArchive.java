package com.pennywise.expense;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "expense_monthly_archives")
public class ExpenseMonthlyArchive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sourceExpenseId;

    @Column(nullable = false)
    private Long userId;

    @Column
    private Long budgetId;

    @Column
    private Long categoryId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private Instant expenseDate;

    @Column(nullable = false)
    private String billingMonth;

    @Column
    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant archivedAt = Instant.now();

    public ExpenseMonthlyArchive() {
    }

    public ExpenseMonthlyArchive(Long sourceExpenseId, Long userId, Long budgetId, Long categoryId, String title, BigDecimal amount, Instant expenseDate, String billingMonth, String notes) {
        this.sourceExpenseId = sourceExpenseId;
        this.userId = userId;
        this.budgetId = budgetId;
        this.categoryId = categoryId;
        this.title = title;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.billingMonth = billingMonth;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public Long getSourceExpenseId() { return sourceExpenseId; }
    public Long getUserId() { return userId; }
    public Long getBudgetId() { return budgetId; }
    public Long getCategoryId() { return categoryId; }
    public String getTitle() { return title; }
    public BigDecimal getAmount() { return amount; }
    public Instant getExpenseDate() { return expenseDate; }
    public String getBillingMonth() { return billingMonth; }
    public String getNotes() { return notes; }
    public Instant getArchivedAt() { return archivedAt; }
}