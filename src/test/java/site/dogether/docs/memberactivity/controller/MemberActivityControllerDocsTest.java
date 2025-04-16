package site.dogether.docs.memberactivity.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import site.dogether.docs.util.RestDocsSupport;
import site.dogether.memberactivity.controller.MemberActivityController;
import site.dogether.memberactivity.controller.response.GetAllGroupNamesResponse;
import site.dogether.memberactivity.controller.response.GroupNameResponse;

import java.util.List;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MemberActivityControllerDocsTest extends RestDocsSupport {

    @Override
    protected Object initController() {
        return new MemberActivityController();
    }

    @DisplayName("참여중인 챌린지 그룹 목록 조회 API")
    @Test
    void getAllGroupsName() throws Exception {
        final List<GroupNameResponse> groups = List.of(
                new GroupNameResponse(1L, "성욱이와 친구들"),
                new GroupNameResponse(2L, "스콘 먹기 챌린지"),
                new GroupNameResponse(3L, "성욱이의 일기")
        );
        GetAllGroupNamesResponse response = new GetAllGroupNamesResponse(groups);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/my/groups")
                                .header("Authorization", "Bearer access_token")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(createDocument(
                        responseFields(
                                fieldWithPath("code")
                                        .description("응답 코드")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("message")
                                        .description("응답 메시지")
                                        .type(JsonFieldType.STRING),
                                fieldWithPath("groups")
                                        .description("그룹 이름 목록")
                                        .optional()
                                        .type(JsonFieldType.ARRAY),
                                fieldWithPath("data.groups[].id")
                                        .description("챌린지 그룹 id")
                                        .optional()
                                        .type(JsonFieldType.NUMBER),
                                fieldWithPath("data.groups[].name")
                                        .description("그룹 이름")
                                        .optional()
                                        .type(JsonFieldType.STRING))));
    }
}
