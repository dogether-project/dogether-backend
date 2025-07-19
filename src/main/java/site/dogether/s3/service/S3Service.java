package site.dogether.s3.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.dogether.s3.url_generator.PresignedUrlGenerator;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Service {

    private final PresignedUrlGenerator presignedUrlGenerator;

    public List<String> issueS3PresignedUrls(final Long dailyTodoId, final List<String> uploadFileTypes) {
        // TODO : 실제 존재하는 데일리 투두 & 인증 대기 상태인지 검증하는 로직을 추가할지 고민 필요
        return uploadFileTypes.stream()
            .map(uploadFileType -> presignedUrlGenerator.generate(dailyTodoId, uploadFileType))
            .toList();
    }
}
