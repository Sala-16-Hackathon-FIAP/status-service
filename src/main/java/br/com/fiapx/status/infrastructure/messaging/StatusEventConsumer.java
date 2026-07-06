package br.com.fiapx.status.infrastructure.messaging;

import br.com.fiapx.status.application.port.input.StatusUseCase;
import br.com.fiapx.status.domain.model.JobStatusType;
import com.autoflow.rabbit_topic_lib.core.TopicConsumer;
import com.autoflow.rabbit_topic_lib.model.TopicBinding;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class StatusEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(StatusEventConsumer.class);

    static final String EXCHANGE = "fiapx.events";
    static final String QUEUE_UPLOAD = "status.video.upload.completed";
    static final String QUEUE_STARTED = "status.video.processing.started";
    static final String QUEUE_COMPLETED = "status.video.processing.completed";
    static final String QUEUE_FAILED = "status.video.processing.failed";

    private final TopicConsumer consumer;
    private final StatusUseCase statusUseCase;

    public StatusEventConsumer(TopicConsumer consumer, StatusUseCase statusUseCase) {
        this.consumer = consumer;
        this.statusUseCase = statusUseCase;
    }

    @PostConstruct
    public void registerConsumers() {
        consumer.consume(new TopicBinding(EXCHANGE, QUEUE_UPLOAD, "video.upload.completed"),
                VideoUploadCompletedEvent.class, this::handleUploadCompleted);
        consumer.consume(new TopicBinding(EXCHANGE, QUEUE_STARTED, "video.processing.started"),
                ProcessingJobEvent.class, this::handleProcessingStarted);
        consumer.consume(new TopicBinding(EXCHANGE, QUEUE_COMPLETED, "video.processing.completed"),
                ProcessingJobEvent.class, this::handleProcessingCompleted);
        consumer.consume(new TopicBinding(EXCHANGE, QUEUE_FAILED, "video.processing.failed"),
                ProcessingJobEvent.class, this::handleProcessingFailed);
    }

    public void handleUploadCompleted(VideoUploadCompletedEvent event) {
        log.info("Status: upload completed for uploadId={}", event.uploadId());
        statusUseCase.upsertStatus(event.uploadId(), event.userId(), event.filename(),
                JobStatusType.UPLOAD_COMPLETED, null, null, null);
    }

    public void handleProcessingStarted(ProcessingJobEvent event) {
        log.info("Status: processing started for uploadId={}", event.uploadId());
        statusUseCase.upsertStatus(event.uploadId(), event.userId(), event.filename(),
                JobStatusType.PROCESSING_STARTED, event.jobId().toString(), null, null);
    }

    public void handleProcessingCompleted(ProcessingJobEvent event) {
        log.info("Status: processing completed for uploadId={}", event.uploadId());
        statusUseCase.upsertStatus(event.uploadId(), event.userId(), event.filename(),
                JobStatusType.PROCESSING_COMPLETED, event.jobId().toString(), event.resultS3Key(), null);
    }

    public void handleProcessingFailed(ProcessingJobEvent event) {
        log.warn("Status: processing failed for uploadId={}: {}", event.uploadId(), event.errorMessage());
        statusUseCase.upsertStatus(event.uploadId(), event.userId(), event.filename(),
                JobStatusType.PROCESSING_FAILED, event.jobId().toString(), null, event.errorMessage());
    }

    public record VideoUploadCompletedEvent(UUID uploadId, UUID userId, String filename,
                                             String s3Key, String mimeType, LocalDateTime uploadedAt) {}

    public record ProcessingJobEvent(UUID jobId, UUID uploadId, UUID userId, String filename,
                                      String resultS3Key, String status, String errorMessage, LocalDateTime timestamp) {}
}
