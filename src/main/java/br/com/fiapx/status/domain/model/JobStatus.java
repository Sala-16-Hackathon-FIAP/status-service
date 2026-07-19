package br.com.fiapx.status.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record JobStatus(
    UUID id,
    UUID jobId,
    UUID uploadId,
    UUID userId,
    String filename,
    JobStatusType status,
    String resultS3Key,
    String errorMessage,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static JobStatus create(UUID uploadId, UUID userId, String filename, JobStatusType status) {
        return new JobStatus(UUID.randomUUID(), null, uploadId, userId, filename, status,
                null, null, LocalDateTime.now(), LocalDateTime.now());
    }

    public static JobStatus createWithJob(UUID jobId, UUID uploadId, UUID userId, String filename, JobStatusType status) {
        return new JobStatus(UUID.randomUUID(), jobId, uploadId, userId, filename, status,
                null, null, LocalDateTime.now(), LocalDateTime.now());
    }

    public JobStatus withStatus(JobStatusType newStatus) {
        return new JobStatus(id, jobId, uploadId, userId, filename, newStatus,
                resultS3Key, errorMessage, createdAt, LocalDateTime.now());
    }

    public JobStatus withResult(String resultKey) {
        return new JobStatus(id, jobId, uploadId, userId, filename, JobStatusType.PROCESSING_COMPLETED,
                resultKey, null, createdAt, LocalDateTime.now());
    }

    public JobStatus withError(String error) {
        return new JobStatus(id, jobId, uploadId, userId, filename, JobStatusType.PROCESSING_FAILED,
                resultS3Key, error, createdAt, LocalDateTime.now());
    }
}
