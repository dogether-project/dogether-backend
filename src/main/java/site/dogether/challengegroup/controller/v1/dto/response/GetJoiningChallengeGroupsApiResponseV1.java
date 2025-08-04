package site.dogether.challengegroup.controller.v1.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupDto;

import java.util.List;

public record GetJoiningChallengeGroupsApiResponseV1(
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer lastSelectedGroupIndex,
    List<JoiningChallengeGroupDto> joiningChallengeGroups
) {}
