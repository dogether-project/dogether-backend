package site.dogether.challengegroup.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.challengegroup.infrastructure.entity.ChallengeGroupJpaEntity;

public interface ChallengeGroupJpaRepository extends JpaRepository<ChallengeGroupJpaEntity, Long> {

    Optional<ChallengeGroupJpaEntity> findByJoinCode(String joinCode);

    LocalDateTime findEndAtById(Long id);
}
