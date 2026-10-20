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
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class StatementProcessorListener {

    private static final Logger logger = LoggerFactory.getLogger(StatementProcessorListener.class);
    private static final int PDF_RENDER_DPI = 200;
    private static final int MAX_PDF_PAGES = 50;
    private static final long MAX_PAGE_PIXELS = 12_000_000L;
    private static final String IMAGE_FAILURE_MESSAGE = "AI could not extract transactions from this image page.";

    @Autowired
    private StatementUploadRequestRepository uploadRequestRepository;

    @Autowired
    private AiCategorizationService aiCategorizationService;

    @Autowired
    private AiPredictionLogRepository aiPredictionLogRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private S3Client s3Client;

    @Value("${pennywise.s3.bucket:pennywise-csv-links}")
    private String s3Bucket;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void processStatementMessage(StatementMessage message) {
        Long requestId = message.getUploadRequestId();
        Long userId = message.getUserId();
        if (requestId == null || userId == null) {
            logger.error("StatementMessage missing requestId or userId.");
            return;
        }

        logger.info("Received statement processing job for uploadRequestId: {}", requestId);
        StatementUploadRequest request = uploadRequestRepository.findById(requestId).orElse(null);
        if (request == null) {
            logger.error("Upload request {} not found.", requestId);
            return;
        }

        try {
            request.setStatus("PROCESSING");
            uploadRequestRepository.save(request);
            List<Budget> budgets = budgetRepository.findByUserId(userId);
            String budgetContext = budgets.stream()
                    .map(b -> "ID: " + b.getId() + ", Name: " + b.getName())
                    .collect(Collectors.joining("\n"));

            if (message.getPdfS3Key() != null && !message.getPdfS3Key().isBlank()) {
                processPdfFromS3(message, budgetContext);
            } else {
                for (String payload : message.getPayloads()) {
                    processPayload(payload, message.getPayloadType(), budgetContext, userId, requestId);
                }
            }

            request.setStatus("COMPLETED");
            uploadRequestRepository.save(request);
            logger.info("Successfully processed uploadRequestId: {}", requestId);
        } catch (Exception e) {
            logger.error("Error processing uploadRequestId: {}", requestId, e);
            request.setStatus("FAILED");
            uploadRequestRepository.save(request);
        }
    }

    private void processPdfFromS3(StatementMessage message, String budgetContext) throws Exception {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(s3Bucket)
                .key(message.getPdfS3Key())
                .build();

        try (ResponseInputStream<GetObjectResponse> input = s3Client.getObject(request);
             PDDocument document = PDDocument.load(input, MemoryUsageSetting.setupTempFileOnly())) {
            int pageCount = document.getNumberOfPages();
            if (pageCount < 1 || pageCount > MAX_PDF_PAGES) {
                throw new IllegalArgumentException("PDF must contain between 1 and " + MAX_PDF_PAGES + " pages.");
            }

            PDFRenderer renderer = new PDFRenderer(document);
            for (int page = 0; page < pageCount; page++) {
                PDRectangle pageSize = document.getPage(page).getMediaBox();
                long pixelWidth = (long) Math.ceil(pageSize.getWidth() * PDF_RENDER_DPI / 72.0);
                long pixelHeight = (long) Math.ceil(pageSize.getHeight() * PDF_RENDER_DPI / 72.0);
                if (pixelWidth <= 0 || pixelHeight <= 0 || pixelWidth > MAX_PAGE_PIXELS / pixelHeight) {
                    throw new IllegalArgumentException("PDF page dimensions are too large to process safely.");
                }

                BufferedImage image = renderer.renderImageWithDPI(page, PDF_RENDER_DPI, ImageType.RGB);
                String payload;
                try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                    ImageIO.write(image, "png", output);
                    payload = "data:image/png;base64," + Base64.getEncoder().encodeToString(output.toByteArray());
                } finally {
                    image.flush();
                }
                processPayload(payload, StatementMessage.PayloadType.IMAGE, budgetContext,
                        message.getUserId(), message.getUploadRequestId());
            }
        }
    }

    private void processPayload(String payload, StatementMessage.PayloadType payloadType,
                                String budgetContext, Long userId, Long requestId) {
        String aiResponse = null;
        try {
            aiResponse = aiCategorizationService.categorizeTransaction(
                    payload, budgetContext, payloadType, userId, requestId);
            logger.info("RAW AI RESPONSE: {}", aiResponse);
        } catch (Exception e) {
            logger.warn("AI categorization failed: {}", e.getMessage());
        }

        if (payloadType == StatementMessage.PayloadType.IMAGE) {
            processImageResponse(extractJsonArray(cleanJsonResponse(aiResponse)), userId, requestId);
        } else {
            processTextResponse(cleanJsonResponse(aiResponse), payload, userId, requestId);
        }
    }

    private void processImageResponse(String aiResponse, Long userId, Long requestId) {
        boolean createdAny = false;
        if (aiResponse != null && !aiResponse.isBlank()) {
            try {
                List<Map<String, Object>> results = objectMapper.readValue(
                        aiResponse, new TypeReference<List<Map<String, Object>>>() {});
                for (Map<String, Object> result : results) {
                    String rawText = result.get("rawText") != null ? result.get("rawText").toString() : "";
                    String title = result.get("title") != null ? result.get("title").toString() : "";
                    String amount = result.get("amount") != null ? result.get("amount").toString() : "0";
                    String combinedText = " | " + title + " | " + amount + " | " + rawText;
                    Long suggestedBudgetId = result.get("suggestedBudgetId") != null
                            ? Long.valueOf(result.get("suggestedBudgetId").toString()) : null;
                    aiPredictionLogRepository.save(new AiPredictionLog(
                            userId, requestId, combinedText, suggestedBudgetId, "PENDING"));
                    createdAny = true;
                }
            } catch (Exception e) {
                logger.warn("Failed parsing IMAGE AI response: {}", e.getMessage());
            }
        }
        if (!createdAny) {
            // Avoid storing a potentially multi-megabyte Base64 image in the prediction table.
            aiPredictionLogRepository.save(new AiPredictionLog(
                    userId, requestId, IMAGE_FAILURE_MESSAGE, null, "PENDING"));
        }
    }

    private void processTextResponse(String aiResponse, String payload, Long userId, Long requestId) {
        Long suggestedBudgetId = null;
        boolean created = false;
        if (aiResponse != null && !aiResponse.isBlank()) {
            try {
                JsonNode result = objectMapper.readTree(aiResponse);
                JsonNode suggestedNode = result.get("suggestedBudgetId");
                if (suggestedNode != null && !suggestedNode.isNull()) {
                    suggestedBudgetId = Long.valueOf(suggestedNode.asText());
                }
                aiPredictionLogRepository.save(new AiPredictionLog(
                        userId, requestId, payload, suggestedBudgetId, "PENDING"));
                created = true;
            } catch (Exception e) {
                logger.warn("Failed parsing TEXT AI response: {}", e.getMessage());
            }
        }
        if (!created) {
            aiPredictionLogRepository.save(new AiPredictionLog(
                    userId, requestId, payload, null, "PENDING"));
        }
    }

    /** Removes markdown wrappers around JSON responses. */
    private String cleanJsonResponse(String response) {
        if (response == null || response.isBlank()) {
            return response;
        }
        response = response.trim();
        if (response.startsWith("```json")) {
            response = response.substring(7).trim();
        }
        if (response.startsWith("```")) {
            response = response.substring(3).trim();
        }
        if (response.endsWith("```")) {
            response = response.substring(0, response.length() - 3).trim();
        }
        return response;
    }

    /**
     * Vision models sometimes wrap their JSON array in a short explanation despite the prompt.
     * Extract the first complete top-level array while respecting quoted brackets and escapes.
     */
    private String extractJsonArray(String response) {
        if (response == null || response.isBlank()) {
            return response;
        }

        int start = response.indexOf('[');
        if (start < 0) {
            return response;
        }

        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int i = start; i < response.length(); i++) {
            char current = response.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == '"') {
                    inString = false;
                }
                continue;
            }

            if (current == '"') {
                inString = true;
            } else if (current == '[') {
                depth++;
            } else if (current == ']') {
                depth--;
                if (depth == 0) {
                    return response.substring(start, i + 1);
                }
            }
        }

        // Leave malformed or truncated output intact so Jackson emits a useful parse error.
        return response;
    }
}
