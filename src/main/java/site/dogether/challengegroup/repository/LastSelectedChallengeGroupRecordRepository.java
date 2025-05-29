package site.dogether.challengegroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.challengegroup.entity.LastSelectedChallengeGroupRecord;
import site.dogether.member.entity.Member;

import java.util.Optional;

public interface LastSelectedChallengeGroupRecordRepository extends JpaRepository<LastSelectedChallengeGroupRecord, Long> {

    Optional<LastSelectedChallengeGroupRecord> findByMember(Member member);
}
