package site.dogether.challengegroup.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.exception.*;
import site.dogether.challengegroup.repository.ChallengeGroupMemberRepository;
import site.dogether.member.entity.Member;

@Service
@RequiredArgsConstructor
public class ChallengeGroupPolicy {

    private static final int MAX_CHALLENGE_GROUP_COUNT = 5;

    private final ChallengeGroupMemberRepository challengeGroupMemberRepository;

    public void validateChallengeGroupMaxCount(final Member member) {
        final int joiningGroupCount = challengeGroupMemberRepository.countNotFinishedGroupByMemberId(member.getId());
        if (joiningGroupCount >= MAX_CHALLENGE_GROUP_COUNT) {
            throw new JoiningChallengeGroupMaxCountException(
                String.format("참여할 수 있는 그룹은 최대 "+ MAX_CHALLENGE_GROUP_COUNT + "개입니다. " +
                                "(memberId: %d), (joiningGroupCount %d): "
                    , member.getId(), joiningGroupCount));
        }
    }

    public void validateChallengeGroupHasMaximumMember(final ChallengeGroup challengeGroup) {
        final int maximumMemberCount = challengeGroup.getMaximumMemberCount();
        final int currentMemberCount = challengeGroupMemberRepository.countByChallengeGroup(challengeGroup);
        if (currentMemberCount >= maximumMemberCount) {
            throw new JoiningChallengeGroupAlreadyFullMemberException(
                String.format("그룹 정원 초과입니다. (currentMemberCount : %d, maximumMemberCount : %d)",
                    currentMemberCount, maximumMemberCount));
        }
    }

    public void validateMemberInSameChallengeGroup(final ChallengeGroup challengeGroup, final Member joinMember) {
        if (challengeGroupMemberRepository.existsByChallengeGroupAndMember(challengeGroup, joinMember)) {
            throw new AlreadyJoinChallengeGroupException(
                    String.format("이미 참여 중인 그룹입니다. (memberId: %d), groupId : %d)",
                            joinMember.getId(), challengeGroup.getId()));
        }
    }

    public void validateChallengeGroupNotFinished(final ChallengeGroup joiningGroup) {
        if (joiningGroup.isFinished()) {
            throw new FinishedChallengeGroupException(
                String.format("이미 종료된 그룹입니다. (groupId: %d)", joiningGroup.getId()));
        }
    }

    public void validateChallengeGroupIsRunning(final ChallengeGroup challengeGroup) {
        if (!challengeGroup.isRunning()) {
            throw new NotRunningChallengeGroupException(String.format("현재 진행중인 챌린지 그룹이 아닙니다. " +
                    "(%s)", challengeGroup));
        }
    }

    public void validateMemberIsInChallengeGroup(final ChallengeGroup challengeGroup, final Member member) {
        if (!challengeGroupMemberRepository.existsByChallengeGroupAndMember(challengeGroup, member)) {
            throw new MemberNotInChallengeGroupException(String.format("사용자가 요청한 챌린지 그룹에 참여중이지 않습니다. " +
                    "(%s) (%s)", challengeGroup, member));
        }
    }
}
