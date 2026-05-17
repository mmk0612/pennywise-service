package com.pennywise.ai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiPredictionLogRepository extends JpaRepository<AiPredictionLog, Long> {
    List<AiPredictionLog> findByUploadRequestId(Long uploadRequestId);
}
