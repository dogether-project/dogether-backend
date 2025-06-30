package site.dogether.docs.appinfo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import site.dogether.appinfo.controller.AppInfoController;
import site.dogether.appinfo.service.AppInfoService;
import site.dogether.docs.util.RestDocsSupport;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("앱 정보 API 문서화 테스트")
public class AppInfoControllerDocsTest extends RestDocsSupport {

    private final AppInfoService appInfoService = mock(AppInfoService.class);

    @Override
    protected Object initController() {
        return new AppInfoController(appInfoService);
    }

    @DisplayName("앱 강제 업데이트 필요 여부 조회 API")
    @Test
    void forceUpdateCheck() throws Exception {
        final String requestAppVersion = "1.0.2";
        given(appInfoService.forceUpdateCheck(requestAppVersion))
            .willReturn(true);

        mockMvc.perform(
                get("/api/app-info/force-update-check")
                    .param("app-version", requestAppVersion)
                    .header("Authorization", "Bearer access_token")
                    .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(createDocument(
                queryParameters(
                    parameterWithName("app-version")
                        .description("강제 업데이트 필요 여부를 확인할 앱 버전")
                        .attributes(constraints("Semantic Versioning 형식의 앱 버전 정보만 요청 가능 (ex. 1.0.1"))),
                responseFields(
                    fieldWithPath("code")
                        .description("응답 코드")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("message")
                        .description("응답 메시지")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.forceUpdateRequired")
                        .description("앱 강제 업데이트 필요 여부")
                        .type(JsonFieldType.BOOLEAN))));
    }
}
