package site.dogether.dailytodo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupMember;
import site.dogether.challengegroup.repository.ChallengeGroupMemberRepository;
import site.dogether.common.util.random.RandomGenerator;
import site.dogether.member.entity.Member;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class RandomReviewerPicker implements ReviewerPicker {

    private final ChallengeGroupMemberRepository challengeGroupMemberRepository;
    private final RandomGenerator randomGenerator;

    @Override
    public Optional<Member> pickReviewerInChallengeGroup(final ChallengeGroup challengeGroup, final Member excludedMember) {
        final List<ChallengeGroupMember> reviewerCandidates = challengeGroupMemberRepository.findAllByChallengeGroupAndMemberNot(challengeGroup, excludedMember);

        if (reviewerCandidates.isEmpty()) {
            log.info("챌린지 그룹에 검사를 수행할 수 있는 인원이 존재하지 않습니다. ({})", challengeGroup);
            return Optional.empty();
        }

        final int randomIndex = randomGenerator.generateNumberInRange(0, reviewerCandidates.size() - 1);
        final Member result = reviewerCandidates.get(randomIndex).getMember();

        log.info("챌린지 그룹 내 검사자가 선정되었습니다. {} {}", challengeGroup, result);
        return Optional.of(result);
    }
}
