package site.dogether.challengegroup.controller.response;

import java.util.List;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupName;

public record GetJoiningChallengeGroupNamesResponse(List<JoiningChallengeGroupName> joiningChallengeGroupNames) {
}
