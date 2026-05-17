package com.pennywise.statement;

import java.math.BigDecimal;
import java.time.Instant;

public class ConfirmedExpenseDTO {
    private Long uploadRequestId;
    private Long predictionLogId;
    private Long finalBudgetId;
    private Boolean rejected;
    private String title;
    private BigDecimal amount;
    private Instant date;

    public Long getUploadRequestId() { return uploadRequestId; }
    public void setUploadRequestId(Long uploadRequestId) { this.uploadRequestId = uploadRequestId; }

    public Long getPredictionLogId() { return predictionLogId; }
    public void setPredictionLogId(Long predictionLogId) { this.predictionLogId = predictionLogId; }

    public Long getFinalBudgetId() { return finalBudgetId; }
    public void setFinalBudgetId(Long finalBudgetId) { this.finalBudgetId = finalBudgetId; }

    public Boolean getRejected() { return rejected; }
    public void setRejected(Boolean rejected) { this.rejected = rejected; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Instant getDate() { return date; }
    public void setDate(Instant date) { this.date = date; }
}
