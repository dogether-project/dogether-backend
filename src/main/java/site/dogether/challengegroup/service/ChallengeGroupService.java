package site.dogether.challengegroup.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.dogether.challengegroup.controller.request.CreateChallengeGroupRequest;
import site.dogether.challengegroup.domain.ChallengeGroup;
import site.dogether.challengegroup.infrastructure.entity.ChallengeGroupJpaEntity;
import site.dogether.challengegroup.infrastructure.repository.ChallengeGroupJpaRepository;

@RequiredArgsConstructor
@Service
public class ChallengeGroupService {

    private final ChallengeGroupJpaRepository challengeGroupJpaRepository;

    public String createChallengeGroup(final CreateChallengeGroupRequest request) {
        ChallengeGroup challengeGroup = new ChallengeGroup(
                request.name(),
                request.maximumMemberCount(),
                request.startAt(),
                request.durationOption(),
                request.maximumTodoCount()
        );

        final ChallengeGroupJpaEntity challengeGroupJpaEntity = ChallengeGroupJpaEntity.from(challengeGroup);
        challengeGroup = challengeGroupJpaRepository.save(challengeGroupJpaEntity).toDomain();

        return challengeGroup.getJoinCode();
    }
}
