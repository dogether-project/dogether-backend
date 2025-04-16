package site.dogether.challengegroup.controller.response;

import java.util.List;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupDto;

public record GetJoiningChallengeGroupsResponse(List<JoiningChallengeGroupDto> joiningChallengeGroups) {
}
