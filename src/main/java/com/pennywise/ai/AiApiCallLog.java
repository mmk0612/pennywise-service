package com.pennywise.ai;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ai_api_call_logs")
public class AiApiCallLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String requestBody;

    @Column(columnDefinition = "TEXT")
    private String responseBody;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public AiApiCallLog() {}

    public AiApiCallLog(String requestBody, String responseBody) {
        this.requestBody = requestBody;
        this.responseBody = responseBody;
        this.createdAt = Instant.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRequestBody() { return requestBody; }
    public void setRequestBody(String requestBody) { this.requestBody = requestBody; }

    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
