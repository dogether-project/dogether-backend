package site.dogether.docs.s3;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import site.dogether.docs.util.RestDocsSupport;
import site.dogether.s3.controller.S3Controller;
import site.dogether.s3.controller.request.IssueS3PresignedUrlsRequest;
import site.dogether.s3.service.S3Service;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static site.dogether.docs.util.DocumentLinkGenerator.DocUrl.S3_UPLOAD_FILE_TYPE;
import static site.dogether.docs.util.DocumentLinkGenerator.generateLink;

@DisplayName("S3 API 문서화 테스트")
public class S3ControllerDocsTest extends RestDocsSupport {

    private final S3Service s3Service = mock(S3Service.class);

    @Override
    protected Object initController() {
        return new S3Controller(s3Service);
    }

    @DisplayName("S3 Presigned Url 생성 API")
    @Test
    void issueS3PresignedUrls() throws Exception {
        final IssueS3PresignedUrlsRequest request = new IssueS3PresignedUrlsRequest(
            1L,
            List.of("IMAGE", "IMAGE", "IMAGE")
        );
        final List<String> s3PresignedUrls = List.of(
            "https://S3-presigned-url1",
            "https://S3-presigned-url2",
            "https://S3-presigned-url3"
        );

        given(s3Service.issueS3PresignedUrls(any(), any()))
            .willReturn(s3PresignedUrls);

        mockMvc.perform(
                post("/api/s3/presigned-urls")
                    .header("Authorization", "Bearer access_token")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(convertToJson(request)))
            .andExpect(status().isOk())
            .andDo(createDocument(
                requestFields(
                    fieldWithPath("dailyTodoId")
                        .description("인증할 데일리 투두 id")
                        .type(JsonFieldType.NUMBER)
                        .attributes(constraints("인증 대기 상태인 데일리 투두 id만 입력 가능")),
                    fieldWithPath("uploadFileTypes")
                        .description(generateLink(S3_UPLOAD_FILE_TYPE))
                        .type(JsonFieldType.ARRAY)
                        .attributes(constraints("요청 리스트 순서에 맞춰 presigned url이 배열로 응답되니 순서 유지를 신경쓸 것. 시스템에서 정의한 타입만 요청 가능."))),
                responseFields(
                    fieldWithPath("code")
                        .description("응답 코드")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("message")
                        .description("응답 메시지")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.presignedUrls")
                        .description("S3 Presigned Url 리스트")
                        .type(JsonFieldType.ARRAY))));
    }
}
