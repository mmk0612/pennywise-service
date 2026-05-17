package com.pennywise.subscription;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "subscriptions")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String plan;

    @Column(nullable = false)
    private String status;

    @Column
    private String provider;

    @Column
    private String providerReference;

    @Column
    private Instant renewalDate;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public Subscription() {}

    public Subscription(Long id, Long userId, String plan, String status, String provider, String providerReference, Instant renewalDate, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.plan = plan;
        this.status = status;
        this.provider = provider;
        this.providerReference = providerReference;
        this.renewalDate = renewalDate;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getProviderReference() { return providerReference; }
    public void setProviderReference(String providerReference) { this.providerReference = providerReference; }
    public Instant getRenewalDate() { return renewalDate; }
    public void setRenewalDate(Instant renewalDate) { this.renewalDate = renewalDate; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
