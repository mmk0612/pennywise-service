package com.pennywise.expense;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import com.pennywise.statement.ConfirmedExpenseDTO;
import com.pennywise.statement.StatementUploadRequestRepository;
import com.pennywise.ai.AiPredictionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;

@Service
@Transactional
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final ExpenseMonthlyArchiveRepository expenseMonthlyArchiveRepository;
    
    @Autowired
    private AiPredictionLogRepository aiPredictionLogRepository;

    @Autowired
    private StatementUploadRequestRepository statementUploadRequestRepository;

    public ExpenseService(ExpenseRepository expenseRepository, ExpenseMonthlyArchiveRepository expenseMonthlyArchiveRepository) {
        this.expenseRepository = expenseRepository;
        this.expenseMonthlyArchiveRepository = expenseMonthlyArchiveRepository;
    }

    public List<ExpenseDTO.ExpenseResponse> saveConfirmedExpenses(List<ConfirmedExpenseDTO> confirmedExpenses, Long userId) {
        Set<Long> uploadRequestIdsToConfirm = new HashSet<>();
        List<ExpenseDTO.ExpenseResponse> responses = new java.util.ArrayList<>();

        for (ConfirmedExpenseDTO dto : confirmedExpenses) {
            if (dto.getUploadRequestId() != null) {
                uploadRequestIdsToConfirm.add(dto.getUploadRequestId());
            }

            if (Boolean.TRUE.equals(dto.getRejected())) {
                Long predictionLogId = dto.getPredictionLogId();
                if (predictionLogId != null) {
                    aiPredictionLogRepository.findById(predictionLogId).ifPresent(aiLog -> {
                        uploadRequestIdsToConfirm.add(aiLog.getUploadRequestId());
                        aiLog.setStatus("REJECTED");
                        aiLog.setFinalBudgetId(null);
                        aiLog.setUserEdited(false);
                        aiPredictionLogRepository.save(aiLog);
                    });
                }
                continue;
            }

            Expense expense = new Expense();
            expense.setUserId(userId);
            expense.setBudgetId(dto.getFinalBudgetId());
            expense.setTitle(dto.getTitle());
            expense.setAmount(dto.getAmount());
            expense.setExpenseDate(dto.getDate());
            expense.setBillingMonth(resolveBillingMonth(null, dto.getDate()));
            expense.setNotes("Imported from statement");
            
            Expense savedExpense = expenseRepository.save(expense);
            
            Long predictionLogId = dto.getPredictionLogId();
            if (predictionLogId != null) {
                aiPredictionLogRepository.findById(predictionLogId).ifPresent(aiLog -> {
                    uploadRequestIdsToConfirm.add(aiLog.getUploadRequestId());
                    aiLog.setStatus("CONFIRMED");
                    aiLog.setFinalBudgetId(dto.getFinalBudgetId());
                    // Check if user changed the AI's suggestion
                    if (aiLog.getSuggestedBudgetId() != null && !aiLog.getSuggestedBudgetId().equals(dto.getFinalBudgetId())) {
                        aiLog.setUserEdited(true);
                    } else if (aiLog.getSuggestedBudgetId() == null && dto.getFinalBudgetId() != null) {
                        aiLog.setUserEdited(true);
                    } else {
                        aiLog.setUserEdited(false);
                    }
                    aiPredictionLogRepository.save(aiLog);
                });
            }

            responses.add(toExpenseResponse(savedExpense));
        }

        markUploadRequestsConfirmed(uploadRequestIdsToConfirm);
        return responses;
    }

    private void markUploadRequestsConfirmed(Set<Long> uploadRequestIds) {
        uploadRequestIds.forEach(uploadRequestId -> {
            if (uploadRequestId == null) {
                return;
            }

            statementUploadRequestRepository.findById(uploadRequestId).ifPresent(request -> {
                request.setStatus("CONFIRMED");
                statementUploadRequestRepository.save(request);
            });
        });
    }

    public ExpenseDTO.ExpenseResponse createExpense(Long userId, ExpenseDTO.CreateExpenseRequest request) {
        Expense expense = new Expense();
        expense.setUserId(userId);
        expense.setTitle(request.title());
        expense.setAmount(request.amount());
        expense.setExpenseDate(request.expenseDate());
        expense.setBillingMonth(resolveBillingMonth(request.billingMonth(), request.expenseDate()));
        expense.setBudgetId(request.budgetId());
        expense.setCategoryId(request.categoryId());
        expense.setNotes(request.notes());

        expense = expenseRepository.save(expense);
        return toExpenseResponse(expense);
    }

    public ExpenseDTO.ExpenseResponse getExpenseById(Long id, Long userId) {
        Expense expense = expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));
        return toExpenseResponse(expense);
    }

    public List<ExpenseDTO.ExpenseResponse> getUserExpenses(Long userId, String billingMonth) {
        String targetMonth = normalizeBillingMonth(billingMonth);
        List<Expense> expenses = targetMonth.equals(currentMonthKey())
                ? expenseRepository.findByUserIdAndBillingMonth(userId, targetMonth)
                : expenseMonthlyArchiveRepository.findByUserIdAndBillingMonth(userId, targetMonth).stream()
                        .map(this::toExpenseEntity)
                        .collect(Collectors.toList());

        return expenses.stream()
                .map(this::toExpenseResponse)
                .collect(Collectors.toList());
    }

    public List<ExpenseDTO.ExpenseResponse> getBudgetExpenses(Long userId, Long budgetId) {
        return expenseRepository.findByUserIdAndBudgetId(userId, budgetId)
                .stream()
                .map(this::toExpenseResponse)
                .collect(Collectors.toList());
    }

    public ExpenseDTO.ExpenseResponse updateExpense(Long id, Long userId, ExpenseDTO.UpdateExpenseRequest request) {
        Expense expense = expenseRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        expense.setTitle(request.title());
        expense.setAmount(request.amount());
        expense.setExpenseDate(request.expenseDate());
        expense.setBillingMonth(resolveBillingMonth(request.billingMonth(), request.expenseDate()));
        expense.setBudgetId(request.budgetId());
        expense.setCategoryId(request.categoryId());
        expense.setNotes(request.notes());
        expense.setUpdatedAt(java.time.Instant.now());

        expense = expenseRepository.save(expense);
        return toExpenseResponse(expense);
    }

    public void deleteExpense(Long id, Long userId) {
        if (!expenseRepository.existsByIdAndUserId(id, userId)) {
            throw new IllegalArgumentException("Expense not found");
        }
        expenseRepository.findByIdAndUserId(id, userId).ifPresent(expenseRepository::delete);
    }

    private ExpenseDTO.ExpenseResponse toExpenseResponse(Expense expense) {
        return new ExpenseDTO.ExpenseResponse(
                expense.getId(),
                expense.getUserId(),
                expense.getTitle(),
                expense.getAmount(),
                expense.getExpenseDate(),
                expense.getBillingMonth(),
                expense.getBudgetId(),
                expense.getCategoryId(),
                expense.getNotes(),
                expense.getCreatedAt(),
                expense.getUpdatedAt()
        );
    }

    private String resolveBillingMonth(String explicitBillingMonth, Instant referenceDate) {
        if (explicitBillingMonth != null && !explicitBillingMonth.isBlank()) {
            return explicitBillingMonth;
        }

        Instant effectiveDate = referenceDate != null ? referenceDate : Instant.now();
        return YearMonth.from(effectiveDate.atZone(ZoneOffset.UTC)).toString();
    }

    private String normalizeBillingMonth(String billingMonth) {
        return billingMonth != null && !billingMonth.isBlank() ? billingMonth : currentMonthKey();
    }

    private String currentMonthKey() {
        return YearMonth.now(ZoneOffset.UTC).toString();
    }

    private Expense toExpenseEntity(ExpenseMonthlyArchive archive) {
        Expense expense = new Expense();
        expense.setId(archive.getId());
        expense.setUserId(archive.getUserId());
        expense.setBudgetId(archive.getBudgetId());
        expense.setCategoryId(archive.getCategoryId());
        expense.setTitle(archive.getTitle());
        expense.setAmount(archive.getAmount());
        expense.setExpenseDate(archive.getExpenseDate());
        expense.setBillingMonth(archive.getBillingMonth());
        expense.setNotes(archive.getNotes());
        return expense;
    }
}
