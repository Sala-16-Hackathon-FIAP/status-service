package br.com.fiapx.status.infrastructure.rest.dto;

import br.com.fiapx.status.domain.model.JobStatus;
import br.com.fiapx.status.domain.model.JobStatusType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JobStatusResponseTest {

    @Test
    void fromDomain_shouldMapAllFields() {
        UUID uploadId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        JobStatus status = new JobStatus(UUID.randomUUID(), jobId, uploadId, UUID.randomUUID(),
                "video.mp4", JobStatusType.PROCESSING_COMPLETED, "res/key", "err", now, now);

        JobStatusResponse response = JobStatusResponse.fromDomain(status);

        assertThat(response.uploadId()).isEqualTo(uploadId);
        assertThat(response.jobId()).isEqualTo(jobId);
        assertThat(response.filename()).isEqualTo("video.mp4");
        assertThat(response.status()).isEqualTo(JobStatusType.PROCESSING_COMPLETED);
        assertThat(response.resultS3Key()).isEqualTo("res/key");
        assertThat(response.errorMessage()).isEqualTo("err");
        assertThat(response.updatedAt()).isEqualTo(now);
    }
}
