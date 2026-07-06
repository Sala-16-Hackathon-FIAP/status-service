package br.com.fiapx.status.infrastructure.rest;

import br.com.fiapx.status.application.port.input.StatusUseCase;
import br.com.fiapx.status.infrastructure.rest.dto.JobStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/status")
@Tag(name = "Status", description = "Video processing job status endpoints")
public class StatusController {

    private final StatusUseCase statusUseCase;

    public StatusController(StatusUseCase statusUseCase) {
        this.statusUseCase = statusUseCase;
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
}
