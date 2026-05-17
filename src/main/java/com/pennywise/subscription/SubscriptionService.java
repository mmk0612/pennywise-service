package com.pennywise.subscription;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class SubscriptionService {
    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public List<SubscriptionDTO.SubscriptionPlan> getAvailablePlans() {
        return Arrays.asList(
                new SubscriptionDTO.SubscriptionPlan("FREE", "Basic budget tracking", "$0"),
                new SubscriptionDTO.SubscriptionPlan("PRO", "Advanced analytics and insights", "$9.99/month"),
                new SubscriptionDTO.SubscriptionPlan("PREMIUM", "Unlimited budgets and exports", "$19.99/month")
        );
    }

    public SubscriptionDTO.SubscriptionResponse getSubscription(Long userId) {
        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("No subscription found"));
        return toSubscriptionResponse(subscription);
    }

    public SubscriptionDTO.SubscriptionResponse createSubscription(Long userId, SubscriptionDTO.CreateSubscriptionRequest request) {
        Subscription existing = subscriptionRepository.findByUserId(userId).orElse(null);
        if (existing != null) {
            existing.setPlan(request.plan());
            existing.setStatus("ACTIVE");
            existing.setProvider(request.provider());
            existing.setProviderReference(request.providerReference());
            existing.setRenewalDate(Instant.now().plus(30, ChronoUnit.DAYS));
            existing.setUpdatedAt(Instant.now());
            existing = subscriptionRepository.save(existing);
        } else {
            Subscription subscription = new Subscription();
            subscription.setUserId(userId);
            subscription.setPlan(request.plan());
            subscription.setStatus("ACTIVE");
            subscription.setProvider(request.provider());
            subscription.setProviderReference(request.providerReference());
            subscription.setRenewalDate(Instant.now().plus(30, ChronoUnit.DAYS));
            existing = subscriptionRepository.save(subscription);
        }
        return toSubscriptionResponse(existing);
    }

    public void cancelSubscription(Long userId) {
        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("No subscription found"));
        subscription.setStatus("CANCELLED");
        subscription.setUpdatedAt(Instant.now());
        subscriptionRepository.save(subscription);
    }

    private SubscriptionDTO.SubscriptionResponse toSubscriptionResponse(Subscription subscription) {
        return new SubscriptionDTO.SubscriptionResponse(
                subscription.getId(),
                subscription.getUserId(),
                subscription.getPlan(),
                subscription.getStatus(),
                subscription.getProvider(),
                subscription.getProviderReference(),
                subscription.getRenewalDate(),
                subscription.getCreatedAt(),
                subscription.getUpdatedAt()
        );
    }
}
