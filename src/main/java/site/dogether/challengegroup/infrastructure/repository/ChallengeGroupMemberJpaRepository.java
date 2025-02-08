package site.dogether.challengegroup.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.challengegroup.domain.ChallengeGroupStatus;
import site.dogether.challengegroup.infrastructure.entity.ChallengeGroupJpaEntity;
import site.dogether.challengegroup.infrastructure.entity.ChallengeGroupMemberJpaEntity;
import site.dogether.member.infrastructure.entity.MemberJpaEntity;

import java.util.List;
import java.util.Optional;

public interface ChallengeGroupMemberJpaRepository extends JpaRepository<ChallengeGroupMemberJpaEntity, Long> {

    Optional<ChallengeGroupMemberJpaEntity> findByChallengeGroup_StatusAndMember(ChallengeGroupStatus challengeGroupStatus, MemberJpaEntity member);

    List<ChallengeGroupMemberJpaEntity> findAllByChallengeGroup(ChallengeGroupJpaEntity challengeGroup);
}
