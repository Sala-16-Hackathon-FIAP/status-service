package br.com.fiapx.status.domain.exception;

import java.util.UUID;

public class JobStatusNotFoundException extends RuntimeException {
    public JobStatusNotFoundException(UUID uploadId) {
        super("Job status not found for upload: " + uploadId);
    }
}
