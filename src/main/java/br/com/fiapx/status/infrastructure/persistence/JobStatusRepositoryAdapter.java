package br.com.fiapx.status.infrastructure.persistence;

import br.com.fiapx.status.application.port.output.JobStatusRepositoryPort;
import br.com.fiapx.status.domain.model.JobStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JobStatusRepositoryAdapter implements JobStatusRepositoryPort {

    private final JobStatusJpaRepository jpaRepository;

    public JobStatusRepositoryAdapter(JobStatusJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public JobStatus save(JobStatus status) {
        return jpaRepository.save(JobStatusEntity.fromDomain(status)).toDomain();
    }

    @Override
    public Optional<JobStatus> findByUploadId(UUID uploadId) {
        return jpaRepository.findByUploadId(uploadId).map(JobStatusEntity::toDomain);
    }

    @Override
    public List<JobStatus> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream().map(JobStatusEntity::toDomain).toList();
    }
}
