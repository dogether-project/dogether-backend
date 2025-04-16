package site.dogether.memberactivity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.dogether.auth.resolver.Authenticated;
import site.dogether.common.controller.response.ApiResponse;
import site.dogether.memberactivity.controller.response.GetAllGroupNamesResponse;
import site.dogether.memberactivity.controller.response.GroupNameResponse;

import java.util.List;

import static site.dogether.memberactivity.controller.response.MemberActivitySuccessCode.GET_All_GROUP_NAMES;

@RequiredArgsConstructor
@RequestMapping("/api/my")
@RestController
public class MemberActivityController {

    @GetMapping("/groups")
    public ResponseEntity<ApiResponse<GetAllGroupNamesResponse>> getAllGroupNames(
            @Authenticated Long memberId
    ) {
        List<GroupNameResponse> groups = List.of(
                new GroupNameResponse(1L, "성욱이와 친구들"),
                new GroupNameResponse(2L, "스콘 먹기 챌린지"),
                new GroupNameResponse(3L, "성욱이의 일기")
        );
        GetAllGroupNamesResponse response = new GetAllGroupNamesResponse(groups);

        return ResponseEntity.ok(ApiResponse.successWithData(GET_All_GROUP_NAMES, response));
    }
}
