package site.dogether.s3.controller.request;

import java.util.List;

public record IssueS3PresignedUrlsRequest(Long dailyTodoId, List<String> uploadFileTypes) {
}
