package com.pennywise.statement;

import java.io.Serializable;
import java.util.List;

public class StatementMessage implements Serializable {

    public enum PayloadType {
        TEXT,
        IMAGE
    }

    private Long uploadRequestId;
    private Long userId;
    private List<String> payloads;
    private PayloadType payloadType;

    public StatementMessage() {}

    public StatementMessage(Long uploadRequestId, Long userId, List<String> payloads, PayloadType payloadType) {
        this.uploadRequestId = uploadRequestId;
        this.userId = userId;
        this.payloads = payloads;
        this.payloadType = payloadType;
    }

    public Long getUploadRequestId() { return uploadRequestId; }
    public void setUploadRequestId(Long uploadRequestId) { this.uploadRequestId = uploadRequestId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public List<String> getPayloads() { return payloads; }
    public void setPayloads(List<String> payloads) { this.payloads = payloads; }

    public PayloadType getPayloadType() { return payloadType; }
    public void setPayloadType(PayloadType payloadType) { this.payloadType = payloadType; }
}
