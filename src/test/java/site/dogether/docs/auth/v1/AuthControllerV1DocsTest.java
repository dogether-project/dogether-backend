package site.dogether.docs.auth.v1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import site.dogether.auth.controller.v1.AuthControllerV1;
import site.dogether.auth.controller.v1.dto.request.LoginApiRequestV1;
import site.dogether.auth.controller.v1.dto.request.WithdrawApiRequestV1;
import site.dogether.auth.service.AuthService;
import site.dogether.docs.util.RestDocsSupport;
import site.dogether.member.service.dto.AuthenticatedMember;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("로그인 & 회원 탈퇴 V1 API 문서화 테스트")
public class AuthControllerV1DocsTest extends RestDocsSupport {

    private final AuthService authService = mock(AuthService.class);

    @Override
    protected Object initController() {
        return new AuthControllerV1(authService);
    }

    @DisplayName("[V1] 애플 로그인 API")
    @Test
    void loginV1() throws Exception {
        final LoginApiRequestV1 request = new LoginApiRequestV1(
            "김영재",
            "idToken"
        );

        given(authService.login(any(LoginApiRequestV1.class)))
                .willReturn(new AuthenticatedMember("김영재", "idToken"));

        mockMvc.perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(convertToJson(request)))
            .andExpect(status().isOk())
            .andDo(createDocument(
                requestFields(
                    fieldWithPath("name")
                        .description("사용자 이름")
                        .type(JsonFieldType.STRING)
                        .optional()
                        .attributes(constraints("OAuth Provider(Apple)상의 사용자 이름")),
                    fieldWithPath("idToken")
                        .description("애플 유저 식별 토큰")
                        .attributes(constraints("애플이 제공하는 id_token"))
                        .type(JsonFieldType.STRING)
                ),
                responseFields(
                    fieldWithPath("code")
                        .description("응답 코드")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("message")
                        .description("응답 메시지")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.name")
                        .description("사용자 이름")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("data.accessToken")
                        .description("JWT")
                        .type(JsonFieldType.STRING))));
    }

    @DisplayName("[V1] 회원 탈퇴 API")
    @Test
    void withdrawV1() throws Exception {
        final WithdrawApiRequestV1 request = new WithdrawApiRequestV1(
            "authorizationCode"
        );

        doNothing().when(authService).withdraw(anyLong(), any(WithdrawApiRequestV1.class));

        mockMvc.perform(
                delete("/api/v1/auth/withdraw")
                    .header("Authorization", "Bearer access_token")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(convertToJson(request)))
            .andExpect(status().isOk())
            .andDo(createDocument(
                requestFields(
                    fieldWithPath("authorizationCode")
                        .description("인가 코드")
                        .type(JsonFieldType.STRING)
                        .attributes(constraints("애플이 제공하는 인가 코드"))
                ),
                responseFields(
                    fieldWithPath("code")
                        .description("응답 코드")
                        .type(JsonFieldType.STRING),
                    fieldWithPath("message")
                        .description("응답 메시지")
                        .type(JsonFieldType.STRING))));
    }
}
