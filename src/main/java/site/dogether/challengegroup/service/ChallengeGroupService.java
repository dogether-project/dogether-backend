package site.dogether.challengegroup.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.controller.request.CreateChallengeGroupRequest;
import site.dogether.challengegroup.domain.ChallengeGroup;
import site.dogether.challengegroup.exception.InvalidChallengeGroupException;
import site.dogether.challengegroup.infrastructure.entity.ChallengeGroupJpaEntity;
import site.dogether.challengegroup.infrastructure.entity.ChallengeGroupMemberJpaEntity;
import site.dogether.challengegroup.infrastructure.repository.ChallengeGroupJpaRepository;
import site.dogether.challengegroup.infrastructure.repository.ChallengeGroupMemberJpaRepository;
import site.dogether.member.infrastructure.entity.MemberJpaEntity;
import site.dogether.member.service.MemberService;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ChallengeGroupService {

    private final ChallengeGroupJpaRepository challengeGroupJpaRepository;
    private final ChallengeGroupMemberJpaRepository challengeGroupMemberJpaRepository;
    private final MemberService memberService;

    @Transactional
    public String createChallengeGroup(final CreateChallengeGroupRequest request, final String token) {
        final MemberJpaEntity groupCreatorJpaEntity = memberService.findMemberEntityByAuthenticationToken(token);
        memberAlreadyInGroup(groupCreatorJpaEntity);

        ChallengeGroup challengeGroup = new ChallengeGroup(
                request.name(),
                request.maximumMemberCount(),
                request.startAt(),
                request.durationOption(),
                request.maximumTodoCount()
        );

        final ChallengeGroupJpaEntity challengeGroupJpaEntity = ChallengeGroupJpaEntity.from(challengeGroup);
        challengeGroup = challengeGroupJpaRepository.save(challengeGroupJpaEntity).toDomain();

        final ChallengeGroupMemberJpaEntity challengeGroupMemberJpaEntity = new ChallengeGroupMemberJpaEntity(
                challengeGroupJpaEntity, groupCreatorJpaEntity
        );
        challengeGroupMemberJpaRepository.save(challengeGroupMemberJpaEntity);

        return challengeGroup.getJoinCode();
    }

    @Transactional
    public void joinChallengeGroup(final String joinCode, final String token) {
        final MemberJpaEntity groupCreatorJpaEntity = memberService.findMemberEntityByAuthenticationToken(token);
        memberAlreadyInGroup(groupCreatorJpaEntity);

        final ChallengeGroupJpaEntity challengeGroupJpaEntity = challengeGroupJpaRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new InvalidChallengeGroupException("존재하지 않는 그룹입니다."));
        final ChallengeGroup joinGroup = challengeGroupJpaEntity.toDomain();

        boolean isFinishedGroup = joinGroup.isFinished();
        if (isFinishedGroup) {
            throw new InvalidChallengeGroupException("이미 종료된 그룹입니다.");
        }

        int maximumMemberCount = joinGroup.getMaximumMemberCount();
        int currentMemberCount = challengeGroupMemberJpaRepository.countByChallengeGroup(challengeGroupJpaEntity);
        if (currentMemberCount >= maximumMemberCount) {
            throw new InvalidChallengeGroupException("그룹 인원이 가득 찼습니다.");
        }

    }

    private void memberAlreadyInGroup(final MemberJpaEntity groupCreatorJpaEntity) {
        final boolean isAlreadyInGroup = challengeGroupMemberJpaRepository.existsByMember(groupCreatorJpaEntity);
        if (isAlreadyInGroup) {
            throw new InvalidChallengeGroupException("이미 그룹에 속해있는 유저입니다.");
        }
    }
}
