package site.dogether.s3.controller.v1.dto.request;

import java.util.List;

public record IssueS3PresignedUrlsApiRequestV1(Long dailyTodoId, List<String> uploadFileTypes) {
}
