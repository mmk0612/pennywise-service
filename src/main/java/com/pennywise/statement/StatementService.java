package com.pennywise.statement;

import com.opencsv.CSVReader;
import com.pennywise.config.RabbitMQConfig;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import com.pennywise.ai.AiPredictionLog;
import com.pennywise.ai.AiPredictionLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class StatementService {

    private static final Logger logger = LoggerFactory.getLogger(StatementService.class);

    @Autowired
    private StatementUploadRequestRepository statementUploadRequestRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AiPredictionLogRepository aiPredictionLogRepository;

    @Autowired
    private S3Client s3Client;

    @Value("${pennywise.s3.bucket:pennywise-csv-links}")
    private String s3Bucket;

    public StatementUploadRequest processStatement(MultipartFile file, Long userId) throws Exception {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";

        if (!filename.endsWith(".csv") && !filename.endsWith(".pdf") && 
            !filename.endsWith(".png") && !filename.endsWith(".jpg") && !filename.endsWith(".jpeg")) {
            throw new UnsupportedOperationException("Only CSV, PDF, and Image files are supported.");
        }

        StatementUploadRequest uploadRequest = new StatementUploadRequest(userId, file.getOriginalFilename());
        uploadRequest = statementUploadRequestRepository.save(uploadRequest);

        // Create a placeholder AI prediction log so the UI shows an entry while processing is pending.
        try {
            AiPredictionLog placeholder = new AiPredictionLog(userId, uploadRequest.getId(), "Queued for AI processing", null, "PENDING");
            aiPredictionLogRepository.save(placeholder);
        } catch (Exception e) {
            logger.warn("Failed to create placeholder AiPredictionLog: {}", e.getMessage());
        }

        List<String> payloads = new ArrayList<>();
        StatementMessage.PayloadType payloadType;

        // Upload the original file to S3 for all supported types so downstream processors
        // can fetch the original if needed. Save the S3 key to the upload request.
        String typePrefix = filename.endsWith(".csv") ? "csv" : filename.endsWith(".pdf") ? "pdf" : "image";
        String key = String.format("%s/%d/%d_%d_%s", typePrefix, userId, uploadRequest.getId(), Instant.now().toEpochMilli(), file.getOriginalFilename());
        try {
            String contentType = "application/octet-stream";
            if (filename.endsWith(".csv")) contentType = "text/csv";
            else if (filename.endsWith(".pdf")) contentType = "application/pdf";
            else if (filename.endsWith(".png")) contentType = "image/png";
            else contentType = "image/jpeg";

            PutObjectRequest por = PutObjectRequest.builder()
                    .bucket(s3Bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();
            s3Client.putObject(por, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            uploadRequest.setS3Key(key);
            // Construct a public S3 URL. Adjust if using a different region or custom domain.
            String s3Url = String.format("https://%s.s3.amazonaws.com/%s", s3Bucket, key);
            uploadRequest.setS3Url(s3Url);
            statementUploadRequestRepository.save(uploadRequest);
            logger.info("Uploaded file to S3: {} (url={})", key, s3Url);
        } catch (Exception e) {
            logger.warn("Failed to upload to S3, continuing processing locally: {}", e.getMessage());
        }

        if (filename.endsWith(".csv")) {
            payloadType = StatementMessage.PayloadType.TEXT;
            try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
                List<String[]> records = csvReader.readAll();
                for (int i = 1; i < records.size(); i++) { // Skip header
                    String[] row = records.get(i);
                    if (row.length >= 3) {
                        payloads.add(String.join(" | ", row));
                    }
                }
            }
        } else if (filename.endsWith(".pdf")) {
            payloadType = StatementMessage.PayloadType.IMAGE;
            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                for (int page = 0; page < document.getNumberOfPages(); ++page) {
                    BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(bim, "png", baos);
                    String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
                    payloads.add("data:image/png;base64," + base64Image);
                }
            }
        } else {
            // PNG, JPG, JPEG
            payloadType = StatementMessage.PayloadType.IMAGE;
            String mimeType = filename.endsWith(".png") ? "image/png" : "image/jpeg";
            byte[] bytes = file.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(bytes);
            payloads.add("data:" + mimeType + ";base64," + base64Image);
        }

        StatementMessage message = new StatementMessage(uploadRequest.getId(), userId, payloads, payloadType);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, message);
        
        logger.info("Published StatementMessage for uploadRequestId: {} of type {}", uploadRequest.getId(), payloadType);

        return uploadRequest;
    }
}
