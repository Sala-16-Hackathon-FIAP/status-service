package br.com.fiapx.status.infrastructure.rest.dto;

import br.com.fiapx.status.domain.model.JobStatus;
import br.com.fiapx.status.domain.model.JobStatusType;

import java.time.LocalDateTime;
import java.util.UUID;

public record JobStatusResponse(
    UUID uploadId,
    UUID jobId,
    String filename,
    JobStatusType status,
    String resultS3Key,
    String errorMessage,
    LocalDateTime updatedAt
) {
    public static JobStatusResponse fromDomain(JobStatus s) {
        return new JobStatusResponse(s.uploadId(), s.jobId(), s.filename(), s.status(),
                s.resultS3Key(), s.errorMessage(), s.updatedAt());
    }
}
