package br.com.fiapx.status.infrastructure.rest.dto;

public record DownloadResponse(
    String downloadUrl,
    String filename,
    int expiresInMinutes
) {}
