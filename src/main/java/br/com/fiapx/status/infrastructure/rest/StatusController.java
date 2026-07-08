package br.com.fiapx.status.infrastructure.rest;

import br.com.fiapx.status.application.port.input.StatusUseCase;
import br.com.fiapx.status.infrastructure.rest.dto.DownloadResponse;
import br.com.fiapx.status.infrastructure.rest.dto.JobStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/status")
@Tag(name = "Status", description = "Video processing job status endpoints")
public class StatusController {

    private final StatusUseCase statusUseCase;
    private final int expirationMinutes;

    public StatusController(StatusUseCase statusUseCase,
                            @Value("${aws.s3.presigned-url-expiration-minutes}") int expirationMinutes) {
        this.statusUseCase = statusUseCase;
        this.expirationMinutes = expirationMinutes;
    }

    @GetMapping
    @Operation(summary = "List all job statuses for the authenticated user")
    public List<JobStatusResponse> listStatuses(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return statusUseCase.getUserStatuses(userId).stream()
                .map(JobStatusResponse::fromDomain).toList();
    }

    @GetMapping("/uploads/{uploadId}")
    @Operation(summary = "Get job status by upload ID")
    public JobStatusResponse getStatus(@PathVariable UUID uploadId) {
        return JobStatusResponse.fromDomain(statusUseCase.getStatusByUploadId(uploadId));
    }

    @GetMapping("/uploads/{uploadId}/download")
    @Operation(summary = "Get a presigned download URL for the processed frames ZIP")
    public DownloadResponse getDownloadUrl(@PathVariable UUID uploadId, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        String url = statusUseCase.getDownloadUrl(uploadId, userId);
        return new DownloadResponse(url, "frames.zip", expirationMinutes);
    }
}
