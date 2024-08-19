package com.giftAndGo.assignment.adapter.repository;


import com.giftAndGo.assignment.domain.model.FileProcessingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FileProcessingRequestLogRepository extends JpaRepository<FileProcessingRequest, UUID> {
}

