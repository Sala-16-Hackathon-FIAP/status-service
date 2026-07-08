package br.com.fiapx.status.infrastructure.s3;

import br.com.fiapx.status.application.port.output.DownloadPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;

@Component
public class S3DownloadAdapter implements DownloadPort {

    private final S3Presigner presigner;
    private final String bucketName;
    private final int expirationMinutes;

    public S3DownloadAdapter(S3Presigner presigner,
                             @Value("${aws.s3.bucket-name}") String bucketName,
                             @Value("${aws.s3.presigned-url-expiration-minutes}") int expirationMinutes) {
        this.presigner = presigner;
        this.bucketName = bucketName;
        this.expirationMinutes = expirationMinutes;
    }

    @Override
    public String generatePresignedUrl(String s3Key) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expirationMinutes))
                .getObjectRequest(getRequest)
                .build();

        return presigner.presignGetObject(presignRequest).url().toString();
    }
}
