package com.pennywise.statement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatementUploadRequestRepository extends JpaRepository<StatementUploadRequest, Long> {
    List<StatementUploadRequest> findByUserIdOrderByCreatedAtDesc(Long userId);
}
