package site.dogether.challengegroup.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupStatus;

public interface ChallengeGroupRepository extends JpaRepository<ChallengeGroup, Long> {

    Optional<ChallengeGroup> findByJoinCode(String joinCode);

    LocalDate findEndAtById(Long id);

    List<ChallengeGroup> findByStatusNot(ChallengeGroupStatus status);
}
