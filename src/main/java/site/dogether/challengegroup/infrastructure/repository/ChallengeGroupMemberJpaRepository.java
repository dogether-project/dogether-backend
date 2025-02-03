package site.dogether.challengegroup.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.challengegroup.infrastructure.entity.ChallengeGroupMemberJpaEntity;

public interface ChallengeGroupMemberJpaRepository extends JpaRepository<ChallengeGroupMemberJpaEntity, Long> {
}
