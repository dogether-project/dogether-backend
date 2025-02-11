package site.dogether.challengegroup.infrastructure.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.challengegroup.infrastructure.entity.ChallengeGroupJpaEntity;
import site.dogether.challengegroup.infrastructure.entity.ChallengeGroupMemberJpaEntity;
import site.dogether.member.infrastructure.entity.MemberJpaEntity;

public interface ChallengeGroupMemberJpaRepository extends JpaRepository<ChallengeGroupMemberJpaEntity, Long> {

    boolean existsByMember(MemberJpaEntity memberJpaEntity);

    int countByChallengeGroup(ChallengeGroupJpaEntity challengeGroupJpaEntity);

    List<ChallengeGroupMemberJpaEntity> findAllByChallengeGroup(ChallengeGroupJpaEntity challengeGroupJpaEntity);
}
