package com.pennywise.statement;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pennywise.ai.AiCategorizationService;
import com.pennywise.ai.AiPredictionLog;
import com.pennywise.ai.AiPredictionLogRepository;
import com.pennywise.budget.Budget;
import com.pennywise.budget.BudgetRepository;
import com.pennywise.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class StatementProcessorListener {

    private static final Logger logger =
            LoggerFactory.getLogger(StatementProcessorListener.class);

    @Autowired
    private StatementUploadRequestRepository uploadRequestRepository;

    @Autowired
    private AiCategorizationService aiCategorizationService;

    @Autowired
    private AiPredictionLogRepository aiPredictionLogRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void processStatementMessage(StatementMessage message) {

        Long requestId = message.getUploadRequestId();
        Long userId = message.getUserId();

        if (requestId == null || userId == null) {
            logger.error("StatementMessage missing requestId or userId.");
            return;
        }

        logger.info(
                "Received statement processing job for uploadRequestId: {}",
                requestId
        );

        StatementUploadRequest request =
                uploadRequestRepository.findById(requestId).orElse(null);

        if (request == null) {
            logger.error("Upload request {} not found.", requestId);
            return;
        }

        try {

            request.setStatus("PROCESSING");
            uploadRequestRepository.save(request);

            List<Budget> budgets =
                    budgetRepository.findByUserId(userId);

            String budgetContext = budgets.stream()
                    .map(b -> "ID: " + b.getId()
                            + ", Name: " + b.getName())
                    .collect(Collectors.joining("\n"));

            for (String payload : message.getPayloads()) {

                String aiResponse = null;

                try {
                    aiResponse =
                            aiCategorizationService
                                    .categorizeTransaction(
                                            payload,
                                            budgetContext,
                                            message.getPayloadType(),
                                            userId,
                                            requestId
                                    );

                    logger.info(
                            "RAW AI RESPONSE: {}",
                            aiResponse
                    );

                } catch (Exception e) {
                    logger.warn(
                            "AI categorization failed: {}",
                            e.getMessage()
                    );
                }

                // Clean markdown wrapped JSON
                aiResponse = cleanJsonResponse(aiResponse);

                if (message.getPayloadType()
                        == StatementMessage.PayloadType.IMAGE) {

                    boolean createdAny = false;

                    if (aiResponse != null
                            && !aiResponse.isBlank()) {

                        try {

                            List<Map<String, Object>> results =
                                    objectMapper.readValue(
                                            aiResponse,
                                            new TypeReference<List<Map<String, Object>>>() {
                                            }
                                    );

                            for (Map<String, Object> result : results) {

                                String rawText =
                                        result.get("rawText") != null
                                                ? result.get("rawText").toString()
                                                : "";

                                String title =
                                        result.get("title") != null
                                                ? result.get("title").toString()
                                                : "";

                                String amount =
                                        result.get("amount") != null
                                                ? result.get("amount").toString()
                                                : "0";

                                String combinedText =
                                        " | " + title
                                                + " | " + amount
                                                + " | " + rawText;

                                Long suggestedBudgetId = null;

                                if (result.get("suggestedBudgetId")
                                        != null) {
                                    suggestedBudgetId =
                                            Long.valueOf(
                                                    result.get(
                                                            "suggestedBudgetId"
                                                    ).toString()
                                            );
                                }

                                AiPredictionLog log =
                                        new AiPredictionLog(
                                                userId,
                                                requestId,
                                                combinedText,
                                                suggestedBudgetId,
                                                "PENDING"
                                        );

                                aiPredictionLogRepository.save(log);
                                createdAny = true;
                            }

                        } catch (Exception e) {

                            logger.warn(
                                    "Failed parsing IMAGE AI response: {}",
                                    e.getMessage()
                            );
                        }
                    }

                    if (!createdAny) {
                        AiPredictionLog fallback =
                                new AiPredictionLog(
                                        userId,
                                        requestId,
                                        payload,
                                        null,
                                        "PENDING"
                                );

                        aiPredictionLogRepository.save(fallback);
                    }

                } else {

                    Long suggestedBudgetId = null;
                    boolean created = false;

                    if (aiResponse != null
                            && !aiResponse.isBlank()) {

                        try {

                            JsonNode result =
                                    objectMapper.readTree(
                                            aiResponse
                                    );

                            JsonNode suggestedNode =
                                    result.get(
                                            "suggestedBudgetId"
                                    );

                            if (suggestedNode != null
                                    && !suggestedNode.isNull()) {

                                suggestedBudgetId =
                                        Long.valueOf(
                                                suggestedNode.asText()
                                        );
                            }

                            AiPredictionLog log =
                                    new AiPredictionLog(
                                            userId,
                                            requestId,
                                            payload,
                                            suggestedBudgetId,
                                            "PENDING"
                                    );

                            aiPredictionLogRepository.save(log);
                            created = true;

                        } catch (Exception e) {

                            logger.warn(
                                    "Failed parsing TEXT AI response: {}",
                                    e.getMessage()
                            );
                        }
                    }

                    if (!created) {

                        AiPredictionLog fallback =
                                new AiPredictionLog(
                                        userId,
                                        requestId,
                                        payload,
                                        null,
                                        "PENDING"
                                );

                        aiPredictionLogRepository.save(fallback);
                    }
                }
            }

            request.setStatus("COMPLETED");
            uploadRequestRepository.save(request);

            logger.info(
                    "Successfully processed uploadRequestId: {}",
                    requestId
            );

        } catch (Exception e) {

            logger.error(
                    "Error processing uploadRequestId: {}",
                    requestId,
                    e
            );

            request.setStatus("FAILED");
            uploadRequestRepository.save(request);
        }
    }

    /**
     * Removes markdown wrappers like:
     * ```json
     * { ... }
     * ```
     */
    private String cleanJsonResponse(String response) {

        if (response == null || response.isBlank()) {
            return response;
        }

        response = response.trim();

        // Remove ```json
        if (response.startsWith("```json")) {
            response = response.substring(7).trim();
        }

        // Remove ```
        if (response.startsWith("```")) {
            response = response.substring(3).trim();
        }

        // Remove ending ```
        if (response.endsWith("```")) {
            response =
                    response.substring(
                            0,
                            response.length() - 3
                    ).trim();
        }

        return response;
    }
}