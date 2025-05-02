package site.dogether.dailytodo.service;

import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.member.entity.Member;

import java.util.Optional;

public interface ReviewerPicker {

    Optional<Member> pickReviewerInChallengeGroup(ChallengeGroup challengeGroup, Member excludedMember);
}
