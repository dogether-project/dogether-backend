package site.dogether.challengegroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupMember;
import site.dogether.member.entity.Member;

import java.util.List;
import java.util.Optional;

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

    @Query("""
            select cgm
            from ChallengeGroupMember cgm
            join fetch cgm.challengeGroup cg
            where cg.status != "FINISHED"
            and cgm.member = :member
            """)
    List<ChallengeGroupMember> findNotFinishedGroupByMember(Member member);

    boolean existsByChallengeGroupAndMember(ChallengeGroup challengeGroup, Member joinMember);

    Optional<ChallengeGroupMember> findByMemberAndChallengeGroup(Member member, ChallengeGroup challengeGroup);

    List<ChallengeGroupMember> findAllByChallengeGroupAndMemberNot(ChallengeGroup challengeGroup, Member excludedMember);
}
