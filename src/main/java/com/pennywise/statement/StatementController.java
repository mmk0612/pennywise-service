package com.pennywise.statement;

import com.pennywise.ai.AiPredictionLog;
import com.pennywise.ai.AiPredictionLogRepository;
import com.pennywise.common.ApiResponse;
import com.pennywise.expense.ExpenseService;
import com.pennywise.security.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/statements")
public class StatementController {

    @Autowired
    private StatementService statementService;
    
    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private StatementUploadRequestRepository statementUploadRequestRepository;

    @Autowired
    private AiPredictionLogRepository aiPredictionLogRepository;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<StatementUploadRequest>> uploadStatement(
            @RequestParam("file") MultipartFile file,
            @AuthenticatedUser Long userId) {
        try {
            StatementUploadRequest request = statementService.processStatement(file, userId);
            return ResponseEntity.ok(ApiResponse.success(request, "Statement uploaded and queued for processing"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to queue statement: " + e.getMessage()));
        }
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<StatementUploadRequest>>> getUploadRequests(@AuthenticatedUser Long userId) {
        List<StatementUploadRequest> requests = statementUploadRequestRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return ResponseEntity.ok(ApiResponse.success(requests, "Fetched upload requests"));
    }

    @GetMapping("/requests/{id}/predictions")
    public ResponseEntity<ApiResponse<List<PendingExpenseDTO>>> getPredictions(
            @PathVariable Long id,
            @AuthenticatedUser Long userId) {
        // Basic ownership check
        StatementUploadRequest request = statementUploadRequestRepository.findById(id).orElse(null);
        if (request == null || !request.getUserId().equals(userId)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Request not found or unauthorized"));
        }

        List<AiPredictionLog> logs = aiPredictionLogRepository.findByUploadRequestId(id);
        List<PendingExpenseDTO> dtos = logs.stream().map(log -> {
            PendingExpenseDTO dto = new PendingExpenseDTO();
            dto.setPredictionLogId(log.getId());
            dto.setRawText(log.getRawExtractedText());
            dto.setSuggestedBudgetId(log.getSuggestedBudgetId());
            
            // Rough parsing for UI display based on raw text
            String[] parts = log.getRawExtractedText().split("\\|");
            if (parts.length >= 3) {
                dto.setTitle(parts[1].trim());
                try {
                    dto.setAmount(new BigDecimal(parts[2].replaceAll("[^\\d.]", "")));
                } catch (Exception e) {
                    dto.setAmount(BigDecimal.ZERO);
                }
            } else {
                dto.setTitle(log.getRawExtractedText());
                dto.setAmount(BigDecimal.ZERO);
            }
            dto.setDate(log.getCreatedAt()); // Fallback date

            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(dtos, "Fetched predictions"));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmExpenses(
            @RequestBody List<ConfirmedExpenseDTO> confirmedExpenses,
            @AuthenticatedUser Long userId) {
        expenseService.saveConfirmedExpenses(confirmedExpenses, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Expenses confirmed and saved successfully"));
    }
}
