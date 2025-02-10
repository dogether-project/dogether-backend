package site.dogether.challengegroup.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.controller.request.CreateChallengeGroupRequest;
import site.dogether.challengegroup.domain.ChallengeGroup;
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
}
