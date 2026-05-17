package com.pennywise.ai;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ai_prediction_logs")
public class AiPredictionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long uploadRequestId;

    @Column(columnDefinition = "TEXT")
    private String rawExtractedText;

    @Column
    private Long suggestedBudgetId;

    @Column
    private Long finalBudgetId;

    @Column
    private Boolean userEdited;

    @Column(nullable = false)
    private String status; // PENDING, CONFIRMED

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public AiPredictionLog() {}

    public AiPredictionLog(Long userId, Long uploadRequestId, String rawExtractedText, Long suggestedBudgetId, String status) {
        this.userId = userId;
        this.uploadRequestId = uploadRequestId;
        this.rawExtractedText = rawExtractedText;
        this.suggestedBudgetId = suggestedBudgetId;
        this.status = status;
        this.createdAt = Instant.now();
        this.userEdited = false;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getUploadRequestId() { return uploadRequestId; }
    public void setUploadRequestId(Long uploadRequestId) { this.uploadRequestId = uploadRequestId; }

    public String getRawExtractedText() { return rawExtractedText; }
    public void setRawExtractedText(String rawExtractedText) { this.rawExtractedText = rawExtractedText; }

    public Long getSuggestedBudgetId() { return suggestedBudgetId; }
    public void setSuggestedBudgetId(Long suggestedBudgetId) { this.suggestedBudgetId = suggestedBudgetId; }

    public Long getFinalBudgetId() { return finalBudgetId; }
    public void setFinalBudgetId(Long finalBudgetId) { this.finalBudgetId = finalBudgetId; }

    public Boolean getUserEdited() { return userEdited; }
    public void setUserEdited(Boolean userEdited) { this.userEdited = userEdited; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
