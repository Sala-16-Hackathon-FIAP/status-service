package br.com.fiapx.status.application.port.output;

import br.com.fiapx.status.domain.model.JobStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobStatusRepositoryPort {
    JobStatus save(JobStatus status);
    Optional<JobStatus> findByUploadId(UUID uploadId);
    List<JobStatus> findByUserId(UUID userId);
}
