package site.dogether.challengegroup.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.challengegroup.infrastructure.entity.ChallengeGroupJpaEntity;

public interface ChallengeGroupJpaRepository extends JpaRepository<ChallengeGroupJpaEntity, Long> {

    boolean existsByJoinCode(String joinCode);
}
