package site.dogether.s3.controller.v1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.dogether.common.controller.dto.response.ApiResponse;
import site.dogether.s3.controller.v1.dto.request.IssueS3PresignedUrlsApiRequestV1;
import site.dogether.s3.controller.v1.dto.response.IssueS3PresignedUrlsApiResponseV1;
import site.dogether.s3.service.S3Service;

import java.util.List;

import static site.dogether.common.controller.dto.response.ApiResponse.success;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/s3")
@RestController
public class S3ControllerV1 {

    private final S3Service s3Service;

    @PostMapping("/presigned-urls")
    public ResponseEntity<ApiResponse<IssueS3PresignedUrlsApiResponseV1>> issueS3PresignedUrls(
        @RequestBody IssueS3PresignedUrlsApiRequestV1 request
    ) {
        final List<String> s3PresignedUrls = s3Service.issueS3PresignedUrls(request.dailyTodoId(), request.uploadFileTypes());
        return ResponseEntity.ok(success(new IssueS3PresignedUrlsApiResponseV1(s3PresignedUrls)));
    }
}
