package br.com.fiapx.status.infrastructure.persistence;

import br.com.fiapx.status.domain.model.JobStatus;
import br.com.fiapx.status.domain.model.JobStatusType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JobStatusEntityTest {

    @Test
    void fromDomainAndToDomain_shouldRoundTrip() {
        UUID id = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID uploadId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        JobStatus original = new JobStatus(id, jobId, uploadId, userId, "video.mp4",
                JobStatusType.PROCESSING_COMPLETED, "res/key", "error msg", now, now);

        JobStatusEntity entity = JobStatusEntity.fromDomain(original);
        JobStatus result = entity.toDomain();

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.jobId()).isEqualTo(jobId);
        assertThat(result.uploadId()).isEqualTo(uploadId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.filename()).isEqualTo("video.mp4");
        assertThat(result.status()).isEqualTo(JobStatusType.PROCESSING_COMPLETED);
        assertThat(result.resultS3Key()).isEqualTo("res/key");
        assertThat(result.errorMessage()).isEqualTo("error msg");
        assertThat(result.createdAt()).isEqualTo(now);
        assertThat(result.updatedAt()).isEqualTo(now);
    }
}
