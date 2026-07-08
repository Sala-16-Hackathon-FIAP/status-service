package br.com.fiapx.status.domain.exception;

import java.util.UUID;

public class DownloadNotReadyException extends RuntimeException {
    public DownloadNotReadyException(UUID uploadId, String currentStatus) {
        super("Download not available for upload " + uploadId + ". Current status: " + currentStatus);
    }
}
