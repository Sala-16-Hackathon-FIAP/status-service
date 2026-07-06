package br.com.fiapx.status.application.port.input;

import br.com.fiapx.status.domain.model.JobStatus;
import br.com.fiapx.status.domain.model.JobStatusType;

import java.util.List;
import java.util.UUID;

public interface StatusUseCase {
    JobStatus upsertStatus(UUID uploadId, UUID userId, String filename, JobStatusType status, String jobId, String resultKey, String error);
    JobStatus getStatusByUploadId(UUID uploadId);
    List<JobStatus> getUserStatuses(UUID userId);
}
