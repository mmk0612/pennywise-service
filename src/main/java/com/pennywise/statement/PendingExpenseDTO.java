package com.pennywise.statement;

import java.math.BigDecimal;
import java.time.Instant;

public class PendingExpenseDTO {
    private String rawText;
    private Long suggestedBudgetId;
    private Long predictionLogId;
    
    // For the UI to show to the user
    private String title;
    private BigDecimal amount;
    private Instant date;

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }

    public Long getSuggestedBudgetId() { return suggestedBudgetId; }
    public void setSuggestedBudgetId(Long suggestedBudgetId) { this.suggestedBudgetId = suggestedBudgetId; }

    public Long getPredictionLogId() { return predictionLogId; }
    public void setPredictionLogId(Long predictionLogId) { this.predictionLogId = predictionLogId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Instant getDate() { return date; }
    public void setDate(Instant date) { this.date = date; }
}
