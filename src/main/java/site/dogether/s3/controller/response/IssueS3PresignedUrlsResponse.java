package site.dogether.s3.controller.response;

import java.util.List;

public record IssueS3PresignedUrlsResponse(List<String> presignedUrls) {
}
