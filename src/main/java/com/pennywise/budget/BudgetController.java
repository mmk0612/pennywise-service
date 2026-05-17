package com.pennywise.budget;

import com.pennywise.common.ApiResponse;
import com.pennywise.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetDTO.BudgetResponse>> createBudget(
            @AuthenticatedUser Long userId,
            @Valid @RequestBody BudgetDTO.CreateBudgetRequest request) {
        BudgetDTO.BudgetResponse response = budgetService.createBudget(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Budget created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BudgetDTO.BudgetResponse>>> getUserBudgets(
            @AuthenticatedUser Long userId) {
        List<BudgetDTO.BudgetResponse> response = budgetService.getUserBudgets(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Budgets retrieved"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetDTO.BudgetResponse>> getBudgetById(
            @PathVariable Long id,
            @AuthenticatedUser Long userId) {
        BudgetDTO.BudgetResponse response = budgetService.getBudgetById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Budget retrieved"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetDTO.BudgetResponse>> updateBudget(
            @PathVariable Long id,
            @AuthenticatedUser Long userId,
            @Valid @RequestBody BudgetDTO.UpdateBudgetRequest request) {
        BudgetDTO.BudgetResponse response = budgetService.updateBudget(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Budget updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(
            @PathVariable Long id,
            @AuthenticatedUser Long userId) {
        budgetService.deleteBudget(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Budget deleted successfully"));
    }
}
