package com.pennywise.subscription;

import com.pennywise.common.ApiResponse;
import com.pennywise.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscription")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<SubscriptionDTO.SubscriptionPlan>>> getAvailablePlans() {
        List<SubscriptionDTO.SubscriptionPlan> response = subscriptionService.getAvailablePlans();
        return ResponseEntity.ok(ApiResponse.success(response, "Plans retrieved"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<SubscriptionDTO.SubscriptionResponse>> getSubscription(
            @AuthenticatedUser Long userId) {
        SubscriptionDTO.SubscriptionResponse response = subscriptionService.getSubscription(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Subscription retrieved"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionDTO.SubscriptionResponse>> createSubscription(
            @AuthenticatedUser Long userId,
            @Valid @RequestBody SubscriptionDTO.CreateSubscriptionRequest request) {
        SubscriptionDTO.SubscriptionResponse response = subscriptionService.createSubscription(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Subscription created successfully"));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> cancelSubscription(
            @AuthenticatedUser Long userId) {
        subscriptionService.cancelSubscription(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Subscription cancelled"));
    }
}
