package site.dogether.docs.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import site.dogether.auth.controller.AuthController;
import site.dogether.auth.controller.request.LoginRequest;
import site.dogether.auth.controller.request.WithdrawRequest;
import site.dogether.docs.util.RestDocsSupport;
import site.dogether.member.service.MemberService;
import site.dogether.member.service.dto.AuthenticatedMember;

@DisplayName("로그인 & 회원 탈퇴 API 문서화 테스트")
public class AuthControllerDocsTest extends RestDocsSupport {

    private final MemberService memberService = mock(MemberService.class);

    @Override
    protected Object initController() {
        return new AuthController(memberService);
    }

    @DisplayName("애플 로그인 API")
    @Test
    void login() throws Exception {
        final LoginRequest request = new LoginRequest(
            "김영재",
            "idTokenidTokenidToken"
        );

        given(memberService.login(any(LoginRequest.class)))
                .willReturn(new AuthenticatedMember("김영재", "accessToken"));

        mockMvc.perform(
                post("/api/auth/login")
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
                        .description("IdentityToken")
                        .attributes(constraints("애플이 제공하는 id_token만 가능"))
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
                        .description("JWT accessToken")
                        .type(JsonFieldType.STRING))));
    }

    @DisplayName("회원 탈퇴 API")
    @Test
    void withdraw() throws Exception {
        final WithdrawRequest request = new WithdrawRequest(
            "authorizationCodeauthorizationCodeauthorizationCode"
        );

//        final AuthenticationToken token = new AuthenticationToken("test-token");
        doNothing().when(memberService).withdraw(anyString(), any(WithdrawRequest.class));

        mockMvc.perform(
                delete("/api/auth/withdraw")
                    .header("Authorization", "Bearer access_token")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(convertToJson(request)))
            .andExpect(status().isOk())
            .andDo(createDocument(
                requestFields(
                    fieldWithPath("authorizationCode")
                        .description("인가 코드")
                        .type(JsonFieldType.STRING)
                        .attributes(constraints("애플이 제공하는 인가 코드만 가능"))
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
