package com.pennywise.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.pennywise.statement.StatementMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiCategorizationService {

    private static final Logger logger = LoggerFactory.getLogger(AiCategorizationService.class);

    @Value("${nvidia.api.key:}")
    private String apiKey;

    @Value("${nvidia.api.url:https://integrate.api.nvidia.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${nvidia.api.model:google/gemma-3n-e2b-it}")
    private String modelName;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AiApiCallLogRepository aiApiCallLogRepository;

    public String categorizeTransaction(String payload, String budgetsContext, StatementMessage.PayloadType payloadType, Long userId, Long uploadRequestId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String bearerToken = apiKey != null ? apiKey : "";
        if (!bearerToken.isEmpty()) {
            headers.setBearerAuth(bearerToken);
        }

        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");

        if (payloadType == StatementMessage.PayloadType.IMAGE) {
            String prompt = "You are a financial assistant. Below is a list of my current budgets:\n" +
                    budgetsContext + "\n\n" +
                    "I am providing an image of a bank statement. Please extract all the transactions from this image. " +
                    "For each transaction, determine the best matching suggestedBudgetId. " +
                    "Return ONLY a JSON array of objects, where each object has: " +
                    "'rawText' (the extracted raw line from the image), " +
                    "'title' (the merchant or description), " +
                    "'amount' (the amount as a number), and " +
                    "'suggestedBudgetId' (the best matching budget ID, or null if no match). " +
                    "Example: [{\"rawText\": \"12/01 WALMART Rs. 45.00\", \"title\": \"GROCERIES\", \"amount\": 45.00, \"suggestedBudgetId\": 5}]";

            List<Map<String, Object>> contentArray = new ArrayList<>();
            contentArray.add(Map.of("type", "text", "text", prompt));
            contentArray.add(Map.of("type", "image_url", "image_url", Map.of("url", payload)));
            userMessage.put("content", contentArray);
        } else {
            String prompt = "You are a financial assistant. Below is a list of my current budgets:\n" +
                    budgetsContext + "\n\n" +
                    "Given this transaction: '" + payload + "', " +
                    "respond with ONLY a JSON object containing the suggestedBudgetId that best matches. " +
                    "If none match well, return null for the ID. Example: {\"suggestedBudgetId\": 5}";
            userMessage.put("content", prompt);
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("messages", List.of(userMessage));
        requestBody.put("temperature", 0.1);
        requestBody.put("max_tokens", 1024);

        String requestJson = null;
        try {
            requestJson = objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            requestJson = requestBody.toString();
        }

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        // If API key is missing, persist the request and a marker response so logs are available
        if (apiKey == null || apiKey.isEmpty()) {
            logger.warn("NVIDIA API key is missing. Skipping AI categorization.");
            try {
                if (aiApiCallLogRepository != null) {
                    AiApiCallLog apiCallLog = new AiApiCallLog(requestJson, "API_KEY_MISSING");
                    aiApiCallLogRepository.save(apiCallLog);
                }
            } catch (Exception e) {
                logger.warn("Failed to persist AI API call log when API key missing: {}", e.getMessage());
            }

            return payloadType == StatementMessage.PayloadType.IMAGE ? "[]" : "{\"suggestedBudgetId\": null}";
        }

        try {
            String targetApiUrl = apiUrl != null ? apiUrl : "";
            ResponseEntity<String> response = restTemplate.postForEntity(targetApiUrl, request, String.class);

            // Serialize response for audit
            String responseJson = null;
            try {
                responseJson = response.getBody();
            } catch (Exception e) {
                responseJson = response.getBody();
            }

            try {
                if (aiApiCallLogRepository != null) {
                    AiApiCallLog apiCallLog = new AiApiCallLog(requestJson, responseJson);
                    aiApiCallLogRepository.save(apiCallLog);
                }
            } catch (Exception e) {
                logger.warn("Failed to persist AI API call log: {}", e.getMessage());
            }

            String responseBody = response.getBody();
            if (response.getStatusCode().is2xxSuccessful() && responseBody != null && !responseBody.isBlank()) {
                JsonNode responseNode = objectMapper.readTree(responseBody);
                JsonNode choicesNode = responseNode.get("choices");
                if (choicesNode != null && choicesNode.isArray() && choicesNode.size() > 0) {
                    JsonNode messageNode = choicesNode.get(0).get("message");
                    if (messageNode != null) {
                        JsonNode contentNode = messageNode.get("content");
                        if (contentNode != null && !contentNode.isNull()) {
                            return contentNode.asText();
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error calling NVIDIA AI API", e);
            // persist request and the exception message as response so we can debug
            try {
                if (aiApiCallLogRepository != null) {
                    AiApiCallLog apiCallLog = new AiApiCallLog(requestJson, "EXCEPTION: " + e.getMessage());
                    aiApiCallLogRepository.save(apiCallLog);
                }
            } catch (Exception ex) {
                logger.warn("Failed to persist AI API call log after exception: {}", ex.getMessage());
            }
        }

        return payloadType == StatementMessage.PayloadType.IMAGE ? "[]" : "{\"suggestedBudgetId\": null}";
    }
}
