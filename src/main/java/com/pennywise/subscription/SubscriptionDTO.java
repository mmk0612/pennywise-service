package com.pennywise.subscription;

import java.time.Instant;

public class SubscriptionDTO {

    public record SubscriptionPlan(
            String name,
            String description,
            String price
    ) {}

    public record CreateSubscriptionRequest(
            String plan,
            String provider,
            String providerReference
    ) {}

    public record SubscriptionResponse(
            Long id,
            Long userId,
            String plan,
            String status,
            String provider,
            String providerReference,
            Instant renewalDate,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
