package br.com.fiapx.status.infrastructure.persistence;

import br.com.fiapx.status.domain.model.JobStatus;
import br.com.fiapx.status.domain.model.JobStatusType;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_statuses")
public class JobStatusEntity {

    @Id
    private UUID id;

    @Column(name = "job_id")
    private UUID jobId;

    @Column(name = "upload_id", nullable = false)
    private UUID uploadId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "filename")
    private String filename;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatusType status;

    @Column(name = "result_s3_key")
    private String resultS3Key;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected JobStatusEntity() {}

    public static JobStatusEntity fromDomain(JobStatus s) {
        JobStatusEntity e = new JobStatusEntity();
        e.id = s.id();
        e.jobId = s.jobId();
        e.uploadId = s.uploadId();
        e.userId = s.userId();
        e.filename = s.filename();
        e.status = s.status();
        e.resultS3Key = s.resultS3Key();
        e.errorMessage = s.errorMessage();
        e.createdAt = s.createdAt();
        e.updatedAt = s.updatedAt();
        return e;
    }

    public JobStatus toDomain() {
        return new JobStatus(id, jobId, uploadId, userId, filename, status,
                resultS3Key, errorMessage, createdAt, updatedAt);
    }
}
