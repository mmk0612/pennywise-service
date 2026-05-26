package com.pennywise.expense;

import com.pennywise.common.ApiResponse;
import com.pennywise.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseDTO.ExpenseResponse>> createExpense(
            @AuthenticatedUser Long userId,
            @Valid @RequestBody ExpenseDTO.CreateExpenseRequest request) {
        ExpenseDTO.ExpenseResponse response = expenseService.createExpense(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Expense created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpenseDTO.ExpenseResponse>>> getUserExpenses(
            @AuthenticatedUser Long userId,
            @RequestParam(value = "month", required = false) String month) {
        List<ExpenseDTO.ExpenseResponse> response = expenseService.getUserExpenses(userId, month);
        return ResponseEntity.ok(ApiResponse.success(response, "Expenses retrieved"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseDTO.ExpenseResponse>> getExpenseById(
            @PathVariable Long id,
            @AuthenticatedUser Long userId) {
        ExpenseDTO.ExpenseResponse response = expenseService.getExpenseById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Expense retrieved"));
    }

    @GetMapping("/budget/{budgetId}")
    public ResponseEntity<ApiResponse<List<ExpenseDTO.ExpenseResponse>>> getBudgetExpenses(
            @PathVariable Long budgetId,
            @AuthenticatedUser Long userId) {
        List<ExpenseDTO.ExpenseResponse> response = expenseService.getBudgetExpenses(userId, budgetId);
        return ResponseEntity.ok(ApiResponse.success(response, "Budget expenses retrieved"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseDTO.ExpenseResponse>> updateExpense(
            @PathVariable Long id,
            @AuthenticatedUser Long userId,
            @Valid @RequestBody ExpenseDTO.UpdateExpenseRequest request) {
        ExpenseDTO.ExpenseResponse response = expenseService.updateExpense(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Expense updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            @PathVariable Long id,
            @AuthenticatedUser Long userId) {
        expenseService.deleteExpense(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Expense deleted successfully"));
    }
}
