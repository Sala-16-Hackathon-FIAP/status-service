package br.com.fiapx.status.application.service;

import br.com.fiapx.status.application.port.input.StatusUseCase;
import br.com.fiapx.status.application.port.output.JobStatusRepositoryPort;
import br.com.fiapx.status.domain.exception.JobStatusNotFoundException;
import br.com.fiapx.status.domain.model.JobStatus;
import br.com.fiapx.status.domain.model.JobStatusType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class StatusService implements StatusUseCase {

    private final JobStatusRepositoryPort repository;

    public StatusService(JobStatusRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public JobStatus upsertStatus(UUID uploadId, UUID userId, String filename,
                                   JobStatusType statusType, String jobId, String resultKey, String error) {
        Optional<JobStatus> existing = repository.findByUploadId(uploadId);
        JobStatus jobStatus;

        if (existing.isEmpty()) {
            UUID jobUUID = jobId != null ? UUID.fromString(jobId) : null;
            jobStatus = JobStatus.createWithJob(jobUUID, uploadId, userId, filename, statusType);
        } else {
            jobStatus = existing.get();
            if (statusType == JobStatusType.PROCESSING_COMPLETED && resultKey != null) {
                jobStatus = jobStatus.withResult(resultKey);
            } else if (statusType == JobStatusType.PROCESSING_FAILED) {
                jobStatus = jobStatus.withError(error);
            } else {
                jobStatus = jobStatus.withStatus(statusType);
            }
        }
        return repository.save(jobStatus);
    }

    @Override
    public JobStatus getStatusByUploadId(UUID uploadId) {
        return repository.findByUploadId(uploadId)
                .orElseThrow(() -> new JobStatusNotFoundException(uploadId));
    }

    @Override
    public List<JobStatus> getUserStatuses(UUID userId) {
        return repository.findByUserId(userId);
    }
}
