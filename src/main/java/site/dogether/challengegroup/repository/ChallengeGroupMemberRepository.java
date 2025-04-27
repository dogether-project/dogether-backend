package site.dogether.challengegroup.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupMember;
import site.dogether.challengegroup.entity.ChallengeGroupStatus;
import site.dogether.member.entity.Member;

public interface ChallengeGroupMemberRepository extends JpaRepository<ChallengeGroupMember, Long> {

    @Query("""
            select count(cgm)
            from ChallengeGroupMember cgm
            join cgm.challengeGroup cg
            where cgm.member.id = :memberId
            and cg.status != "FINISHED"
            """)
    int countNotFinishedGroupByMemberId(Long memberId);

    int countByChallengeGroup(ChallengeGroup challengeGroup);

    List<ChallengeGroupMember> findAllByChallengeGroup(ChallengeGroup challengeGroup);

    Optional<ChallengeGroupMember> findByMember(Member member);

    Optional<ChallengeGroupMember> findByChallengeGroup_StatusAndMember(ChallengeGroupStatus challengeGroupStatus, Member member);

    boolean existsByChallengeGroupAndMember(ChallengeGroup challengeGroup, Member joinMember);
}
