package site.dogether.challengegroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.challengegroup.entity.ChallengeGroup;

import java.time.LocalDate;
import java.util.Optional;

public interface ChallengeGroupRepository extends JpaRepository<ChallengeGroup, Long> {

    Optional<ChallengeGroup> findByJoinCode(String joinCode);

    LocalDate findEndAtById(Long id);
}
