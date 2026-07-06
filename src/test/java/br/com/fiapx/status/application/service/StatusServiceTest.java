package br.com.fiapx.status.application.service;

import br.com.fiapx.status.application.port.output.JobStatusRepositoryPort;
import br.com.fiapx.status.domain.exception.JobStatusNotFoundException;
import br.com.fiapx.status.domain.model.JobStatus;
import br.com.fiapx.status.domain.model.JobStatusType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatusServiceTest {

    @Mock private JobStatusRepositoryPort repository;

    @InjectMocks
    private StatusService statusService;

    private UUID uploadId;
    private UUID userId;
    private UUID jobId;

    @BeforeEach
    void setUp() {
        uploadId = UUID.randomUUID();
        userId = UUID.randomUUID();
        jobId = UUID.randomUUID();
    }

    @Test
    void upsertStatus_shouldCreateNewRecord_whenNoExistingStatus() {
        when(repository.findByUploadId(uploadId)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        JobStatus result = statusService.upsertStatus(uploadId, userId, "video.mp4",
                JobStatusType.UPLOAD_COMPLETED, null, null, null);

        assertThat(result.uploadId()).isEqualTo(uploadId);
        assertThat(result.status()).isEqualTo(JobStatusType.UPLOAD_COMPLETED);
        verify(repository).save(any());
    }

    @Test
    void upsertStatus_shouldUpdateExistingRecord_whenStatusExists() {
        JobStatus existing = new JobStatus(UUID.randomUUID(), null, uploadId, userId, "v.mp4",
                JobStatusType.UPLOAD_COMPLETED, null, null, LocalDateTime.now(), LocalDateTime.now());
        when(repository.findByUploadId(uploadId)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        JobStatus result = statusService.upsertStatus(uploadId, userId, "v.mp4",
                JobStatusType.PROCESSING_STARTED, jobId.toString(), null, null);

        assertThat(result.status()).isEqualTo(JobStatusType.PROCESSING_STARTED);
    }

    @Test
    void upsertStatus_shouldSetResultKey_whenProcessingCompleted() {
        JobStatus existing = new JobStatus(UUID.randomUUID(), jobId, uploadId, userId, "v.mp4",
                JobStatusType.PROCESSING_STARTED, null, null, LocalDateTime.now(), LocalDateTime.now());
        when(repository.findByUploadId(uploadId)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        JobStatus result = statusService.upsertStatus(uploadId, userId, "v.mp4",
                JobStatusType.PROCESSING_COMPLETED, jobId.toString(), "processed/key.zip", null);

        assertThat(result.status()).isEqualTo(JobStatusType.PROCESSING_COMPLETED);
        assertThat(result.resultS3Key()).isEqualTo("processed/key.zip");
    }

    @Test
    void upsertStatus_shouldSetErrorMessage_whenProcessingFailed() {
        JobStatus existing = new JobStatus(UUID.randomUUID(), jobId, uploadId, userId, "v.mp4",
                JobStatusType.PROCESSING_STARTED, null, null, LocalDateTime.now(), LocalDateTime.now());
        when(repository.findByUploadId(uploadId)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        JobStatus result = statusService.upsertStatus(uploadId, userId, "v.mp4",
                JobStatusType.PROCESSING_FAILED, jobId.toString(), null, "FFmpeg error");

        assertThat(result.status()).isEqualTo(JobStatusType.PROCESSING_FAILED);
        assertThat(result.errorMessage()).isEqualTo("FFmpeg error");
    }

    @Test
    void getStatusByUploadId_shouldReturnStatus_whenExists() {
        JobStatus status = new JobStatus(UUID.randomUUID(), jobId, uploadId, userId, "v.mp4",
                JobStatusType.PROCESSING_COMPLETED, "key.zip", null, LocalDateTime.now(), LocalDateTime.now());
        when(repository.findByUploadId(uploadId)).thenReturn(Optional.of(status));

        JobStatus found = statusService.getStatusByUploadId(uploadId);
        assertThat(found).isEqualTo(status);
    }

    @Test
    void getStatusByUploadId_shouldThrow_whenNotFound() {
        when(repository.findByUploadId(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> statusService.getStatusByUploadId(UUID.randomUUID()))
                .isInstanceOf(JobStatusNotFoundException.class);
    }

    @Test
    void getUserStatuses_shouldReturnAllForUser() {
        List<JobStatus> statuses = List.of(
                new JobStatus(UUID.randomUUID(), null, uploadId, userId, "v1.mp4",
                        JobStatusType.PROCESSING_COMPLETED, "key.zip", null, LocalDateTime.now(), LocalDateTime.now()),
                new JobStatus(UUID.randomUUID(), null, UUID.randomUUID(), userId, "v2.mp4",
                        JobStatusType.PROCESSING_STARTED, null, null, LocalDateTime.now(), LocalDateTime.now())
        );
        when(repository.findByUserId(userId)).thenReturn(statuses);

        List<JobStatus> found = statusService.getUserStatuses(userId);
        assertThat(found).hasSize(2);
    }
}
