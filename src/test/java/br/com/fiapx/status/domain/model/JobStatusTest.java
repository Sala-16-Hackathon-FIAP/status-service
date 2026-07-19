package br.com.fiapx.status.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JobStatusTest {

    @Test
    void create_shouldInitializeWithCorrectValues() {
        UUID uploadId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        JobStatus status = JobStatus.create(uploadId, userId, "video.mp4", JobStatusType.UPLOAD_COMPLETED);

        assertThat(status.id()).isNotNull();
        assertThat(status.jobId()).isNull();
        assertThat(status.uploadId()).isEqualTo(uploadId);
        assertThat(status.userId()).isEqualTo(userId);
        assertThat(status.filename()).isEqualTo("video.mp4");
        assertThat(status.status()).isEqualTo(JobStatusType.UPLOAD_COMPLETED);
        assertThat(status.resultS3Key()).isNull();
        assertThat(status.errorMessage()).isNull();
    }

    @Test
    void createWithJob_shouldSetJobId() {
        UUID jobId = UUID.randomUUID();
        UUID uploadId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        JobStatus status = JobStatus.createWithJob(jobId, uploadId, userId, "v.mp4", JobStatusType.PROCESSING_STARTED);

        assertThat(status.jobId()).isEqualTo(jobId);
        assertThat(status.uploadId()).isEqualTo(uploadId);
        assertThat(status.status()).isEqualTo(JobStatusType.PROCESSING_STARTED);
    }

    @Test
    void withStatus_shouldChangeStatus() {
        JobStatus status = JobStatus.create(UUID.randomUUID(), UUID.randomUUID(), "v.mp4", JobStatusType.UPLOAD_COMPLETED);
        JobStatus updated = status.withStatus(JobStatusType.PROCESSING_STARTED);

        assertThat(updated.status()).isEqualTo(JobStatusType.PROCESSING_STARTED);
        assertThat(updated.id()).isEqualTo(status.id());
    }

    @Test
    void withResult_shouldSetResultAndCompleteStatus() {
        JobStatus status = JobStatus.create(UUID.randomUUID(), UUID.randomUUID(), "v.mp4", JobStatusType.PROCESSING_STARTED);
        JobStatus completed = status.withResult("processed/key.zip");

        assertThat(completed.status()).isEqualTo(JobStatusType.PROCESSING_COMPLETED);
        assertThat(completed.resultS3Key()).isEqualTo("processed/key.zip");
        assertThat(completed.errorMessage()).isNull();
    }

    @Test
    void withError_shouldSetErrorAndFailedStatus() {
        JobStatus status = JobStatus.create(UUID.randomUUID(), UUID.randomUUID(), "v.mp4", JobStatusType.PROCESSING_STARTED);
        JobStatus failed = status.withError("FFmpeg crashed");

        assertThat(failed.status()).isEqualTo(JobStatusType.PROCESSING_FAILED);
        assertThat(failed.errorMessage()).isEqualTo("FFmpeg crashed");
    }

    @Test
    void recordAccessors_shouldReturnCorrectValues() {
        UUID id = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID uploadId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        JobStatus status = new JobStatus(id, jobId, uploadId, userId, "test.mp4",
                JobStatusType.PROCESSING_COMPLETED, "res", "err", now, now);

        assertThat(status.id()).isEqualTo(id);
        assertThat(status.jobId()).isEqualTo(jobId);
        assertThat(status.uploadId()).isEqualTo(uploadId);
        assertThat(status.userId()).isEqualTo(userId);
        assertThat(status.filename()).isEqualTo("test.mp4");
        assertThat(status.resultS3Key()).isEqualTo("res");
        assertThat(status.errorMessage()).isEqualTo("err");
        assertThat(status.createdAt()).isEqualTo(now);
        assertThat(status.updatedAt()).isEqualTo(now);
    }
}
