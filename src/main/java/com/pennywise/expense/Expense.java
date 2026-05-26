package com.pennywise.expense;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "expenses")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public Expense() {}

    public Expense(Long id, Long userId, Long budgetId, Long categoryId, String title, BigDecimal amount, Instant expenseDate, String billingMonth, String notes, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.budgetId = budgetId;
        this.categoryId = categoryId;
        this.title = title;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.billingMonth = billingMonth;
        this.notes = notes;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public Expense(Long id, Long userId, Long budgetId, Long categoryId, String title, BigDecimal amount, Instant expenseDate, String notes, Instant createdAt, Instant updatedAt) {
        this(id, userId, budgetId, categoryId, title, amount, expenseDate, null, notes, createdAt, updatedAt);
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getBudgetId() { return budgetId; }
    public void setBudgetId(Long budgetId) { this.budgetId = budgetId; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Instant getExpenseDate() { return expenseDate; }
    public void setExpenseDate(Instant expenseDate) { this.expenseDate = expenseDate; }
    public String getBillingMonth() { return billingMonth; }
    public void setBillingMonth(String billingMonth) { this.billingMonth = billingMonth; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
