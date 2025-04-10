package site.dogether.challengegroup.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.challengegroup.entity.ChallengeGroup;

public interface ChallengeGroupRepository extends JpaRepository<ChallengeGroup, Long> {

    Optional<ChallengeGroup> findByJoinCode(String joinCode);

    LocalDateTime findEndAtById(Long id);
}
