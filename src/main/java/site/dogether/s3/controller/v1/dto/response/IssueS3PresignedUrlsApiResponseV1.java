package site.dogether.s3.controller.v1.dto.response;

import java.util.List;

public record IssueS3PresignedUrlsApiResponseV1(List<String> presignedUrls) {
}
