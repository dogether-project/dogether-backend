package site.dogether.challengegroup.service.dto;

import site.dogether.challengegroup.entity.ChallengeGroupMember;

public record ChallengeGroupMemberWithAchievementRateDto(
    ChallengeGroupMember challengeGroupMember,
    int achievementRate
) {}
