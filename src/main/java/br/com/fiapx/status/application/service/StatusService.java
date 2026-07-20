package br.com.fiapx.status.application.service;

import br.com.fiapx.status.application.port.input.StatusUseCase;
import br.com.fiapx.status.application.port.output.DownloadPort;
import br.com.fiapx.status.application.port.output.JobStatusRepositoryPort;
import br.com.fiapx.status.domain.exception.DownloadNotReadyException;
import br.com.fiapx.status.domain.exception.JobStatusNotFoundException;
import br.com.fiapx.status.domain.model.JobStatus;
import br.com.fiapx.status.domain.model.JobStatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class StatusService implements StatusUseCase {

    private static final Logger log = LoggerFactory.getLogger(StatusService.class);

    private final JobStatusRepositoryPort repository;
    private final DownloadPort downloadPort;

    public StatusService(JobStatusRepositoryPort repository, DownloadPort downloadPort) {
        this.repository = repository;
        this.downloadPort = downloadPort;
    }

    @Override
    public JobStatus upsertStatus(UUID uploadId, UUID userId, String filename,
                                   JobStatusType statusType, String jobId, String resultKey, String error) {
        try {
            return doUpsert(uploadId, userId, filename, statusType, jobId, resultKey, error);
        } catch (DataIntegrityViolationException e) {
            log.debug("Concurrent insert for uploadId={}, retrying as update", uploadId);
            return doUpsert(uploadId, userId, filename, statusType, jobId, resultKey, error);
        }
    }

    private JobStatus doUpsert(UUID uploadId, UUID userId, String filename,
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

    @Override
    public String getDownloadUrl(UUID uploadId, UUID userId) {
        JobStatus status = getStatusByUploadId(uploadId);
        if (!status.userId().equals(userId)) {
            throw new JobStatusNotFoundException(uploadId);
        }
        if (status.status() != JobStatusType.PROCESSING_COMPLETED || status.resultS3Key() == null) {
            throw new DownloadNotReadyException(uploadId, status.status().name());
        }
        return downloadPort.generatePresignedUrl(status.resultS3Key());
    }
}
