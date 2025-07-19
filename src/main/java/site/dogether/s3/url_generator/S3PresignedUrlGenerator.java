package site.dogether.s3.url_generator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import site.dogether.s3.service.S3UploadFileType;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3PresignedUrlGenerator implements PresignedUrlGenerator {

    private static final String TODO_CERTIFICATION_MEDIA_ROOT_DIR_NAME = "daily_todo_certification_media";
    private static final int PRESIGNED_URL_EXPIRATION_MINUTES = 5;

    @Value("${secret.aws.s3.bucket-name}")
    private String bucketName;

    private final S3Presigner s3Presigner;

    @Override
    public String generate(final Long dailyTodoId, final String uploadFileType) {
        return generateS3PresignedUrl(dailyTodoId, uploadFileType);
    }

    private String generateS3PresignedUrl(final Long dailyTodoId, final String uploadFileType) {
        return s3Presigner.presignPutObject(buildPutObjectPresignRequest(dailyTodoId, uploadFileType))
            .url()
            .toExternalForm();
    }

    private PutObjectPresignRequest buildPutObjectPresignRequest(final Long dailyTodoId, final String uploadFileType) {
        final S3UploadFileType s3UploadFileType = S3UploadFileType.valueOf(uploadFileType);
        final String s3PutObjectKey = String.format("%s/%d/%s.%s",
            TODO_CERTIFICATION_MEDIA_ROOT_DIR_NAME,
            dailyTodoId,
            UUID.randomUUID(),
            s3UploadFileType.getExtension()
        );

        log.info("generate s3 put object key: {}", s3PutObjectKey);

        final PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(s3PutObjectKey)
            .contentType(s3UploadFileType.getContentType())
            .build();

        return PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(PRESIGNED_URL_EXPIRATION_MINUTES))
            .putObjectRequest(putObjectRequest)
            .build();
    }
}
