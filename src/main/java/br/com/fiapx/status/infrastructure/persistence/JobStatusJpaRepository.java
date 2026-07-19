package br.com.fiapx.status.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobStatusJpaRepository extends JpaRepository<JobStatusEntity, UUID> {
    Optional<JobStatusEntity> findByUploadId(UUID uploadId);
    List<JobStatusEntity> findByUserId(UUID userId);
}
