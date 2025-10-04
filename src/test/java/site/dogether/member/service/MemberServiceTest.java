package site.dogether.member.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupMember;
import site.dogether.challengegroup.repository.ChallengeGroupMemberRepository;
import site.dogether.challengegroup.repository.ChallengeGroupRepository;
import site.dogether.member.entity.Member;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class MemberServiceTest {

    @Autowired private MemberService memberService;
    @Autowired private ChallengeGroupRepository challengeGroupRepository;
    @Autowired private ChallengeGroupMemberRepository challengeGroupMemberRepository;

    @Test
    void 신규_회원을_저장한다() {
        // given
        String providerId = "providerId";
        String name = "폰트";

        // when
        Member saved = memberService.save(providerId, name);

        // then
        assertThat(saved.getProviderId()).isEqualTo(providerId);
    }

    @Test
    void 이미_가입된_회원이면_저장하지_않고_조회한다() {
        // given
        String providerId = "providerId";
        String name = "폰트";
        Member saved = memberService.save(providerId, name);

        // when
        Member found = memberService.save(providerId, name);

        // then
        assertThat(found.getId()).isEqualTo(saved.getId());
    }

    @Test
    void 탈퇴한_회원이_다시_가입하면_논리_삭제했던_정보를_물리_삭제하고_새로_저장한다() {
        // given
        String providerId = "providerId";
        String name = "폰트";
        Member member = memberService.save(providerId, name);
        LocalDateTime createdAt = LocalDateTime.now();
        ChallengeGroup challengeGroup = challengeGroupRepository.save(ChallengeGroup.create(
                "그룹", 10,
                LocalDate.now(), LocalDate.now().plusDays(3), createdAt
        ));
        ChallengeGroupMember challengeGroupMember = challengeGroupMemberRepository.save(
                new ChallengeGroupMember(challengeGroup, member));

        memberService.delete(member.getId());

        // when
        Member found = memberService.save(providerId, name);

        // then
        assertThat(found.getId()).isNotEqualTo(member.getId());
        assertThat(challengeGroupMemberRepository.findById(challengeGroupMember.getId()))
                .isEmpty();
    }
}
