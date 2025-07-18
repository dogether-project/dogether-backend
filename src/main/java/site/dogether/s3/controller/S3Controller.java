package site.dogether.s3.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.dogether.common.controller.response.ApiResponse;
import site.dogether.s3.controller.request.IssueS3PresignedUrlsRequest;
import site.dogether.s3.controller.response.IssueS3PresignedUrlsResponse;
import site.dogether.s3.service.S3Service;

import java.util.List;

import static site.dogether.common.controller.response.ApiResponse.*;

@RequiredArgsConstructor
@RequestMapping("/api/s3")
@RestController
public class S3Controller {

    private final S3Service s3Service;

    @PostMapping("/presigned-urls")
    public ResponseEntity<ApiResponse<IssueS3PresignedUrlsResponse>> issueS3PresignedUrls(
        @RequestBody IssueS3PresignedUrlsRequest request
    ) {
        final List<String> s3PresignedUrls = s3Service.issueS3PresignedUrls(request.dailyTodoId(), request.uploadFileTypes());
        return ResponseEntity.ok(success(new IssueS3PresignedUrlsResponse(s3PresignedUrls)));
    }
}
