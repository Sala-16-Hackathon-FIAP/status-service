package br.com.fiapx.status.application.port.output;

public interface DownloadPort {
    String generatePresignedUrl(String s3Key);
}
