package br.com.fiapx.status.infrastructure.messaging;

import br.com.fiapx.status.application.port.input.StatusUseCase;
import br.com.fiapx.status.domain.model.JobStatusType;
import com.autoflow.rabbit_topic_lib.core.TopicConsumer;
import com.autoflow.rabbit_topic_lib.model.TopicBinding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StatusEventConsumerTest {

    @Mock private TopicConsumer topicConsumer;
    @Mock private StatusUseCase statusUseCase;

    @InjectMocks
    private StatusEventConsumer consumer;

    @Test
    void registerConsumers_shouldBindAllQueues() {
        consumer.registerConsumers();

        verify(topicConsumer).consume(any(TopicBinding.class),
                eq(StatusEventConsumer.VideoUploadCompletedEvent.class), any());
        verify(topicConsumer, times(3)).consume(any(TopicBinding.class),
                eq(StatusEventConsumer.ProcessingJobEvent.class), any());
    }

    @Test
    void handleUploadCompleted_shouldUpsertWithUploadCompletedStatus() {
        UUID uploadId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        StatusEventConsumer.VideoUploadCompletedEvent event =
                new StatusEventConsumer.VideoUploadCompletedEvent(
                        uploadId, userId, "video.mp4", "s3key", "video/mp4", LocalDateTime.now());

        consumer.handleUploadCompleted(event);

        verify(statusUseCase).upsertStatus(eq(uploadId), eq(userId), eq("video.mp4"),
                eq(JobStatusType.UPLOAD_COMPLETED), isNull(), isNull(), isNull());
    }

    @Test
    void handleProcessingStarted_shouldUpsertWithProcessingStartedStatus() {
        UUID jobId = UUID.randomUUID();
        UUID uploadId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        StatusEventConsumer.ProcessingJobEvent event =
                new StatusEventConsumer.ProcessingJobEvent(
                        jobId, uploadId, userId, "video.mp4", null, "PROCESSING", null, LocalDateTime.now());

        consumer.handleProcessingStarted(event);

        verify(statusUseCase).upsertStatus(eq(uploadId), eq(userId), eq("video.mp4"),
                eq(JobStatusType.PROCESSING_STARTED), eq(jobId.toString()), isNull(), isNull());
    }

    @Test
    void handleProcessingCompleted_shouldUpsertWithResultKey() {
        UUID jobId = UUID.randomUUID();
        UUID uploadId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        StatusEventConsumer.ProcessingJobEvent event =
                new StatusEventConsumer.ProcessingJobEvent(
                        jobId, uploadId, userId, "video.mp4", "processed/key.zip", "COMPLETED", null, LocalDateTime.now());

        consumer.handleProcessingCompleted(event);

        verify(statusUseCase).upsertStatus(eq(uploadId), eq(userId), eq("video.mp4"),
                eq(JobStatusType.PROCESSING_COMPLETED), eq(jobId.toString()), eq("processed/key.zip"), isNull());
    }

    @Test
    void handleProcessingFailed_shouldUpsertWithErrorMessage() {
        UUID jobId = UUID.randomUUID();
        UUID uploadId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        StatusEventConsumer.ProcessingJobEvent event =
                new StatusEventConsumer.ProcessingJobEvent(
                        jobId, uploadId, userId, "video.mp4", null, "FAILED", "FFmpeg error", LocalDateTime.now());

        consumer.handleProcessingFailed(event);

        verify(statusUseCase).upsertStatus(eq(uploadId), eq(userId), eq("video.mp4"),
                eq(JobStatusType.PROCESSING_FAILED), eq(jobId.toString()), isNull(), eq("FFmpeg error"));
    }
}
