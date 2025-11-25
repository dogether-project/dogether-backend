package site.dogether.challengegroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ChallengeGroupRepository extends JpaRepository<ChallengeGroup, Long> {

    Optional<ChallengeGroup> findByJoinCode_Value(String joinCodeValue);

    LocalDate findEndAtById(Long id);

    List<ChallengeGroup> findByStatusNot(ChallengeGroupStatus status);
}
