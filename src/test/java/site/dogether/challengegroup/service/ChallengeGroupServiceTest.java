package site.dogether.challengegroup.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.controller.v1.dto.request.CreateChallengeGroupApiRequestV1;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupMember;
import site.dogether.challengegroup.exception.*;
import site.dogether.challengegroup.fixture.ChallengeGroupFixture;
import site.dogether.challengegroup.repository.ChallengeGroupMemberRepository;
import site.dogether.challengegroup.repository.ChallengeGroupRepository;
import site.dogether.challengegroup.service.dto.JoinChallengeGroupDto;
import site.dogether.challengegroup.service.dto.JoiningChallengeGroupsWithLastSelectedGroupIndexDto;
import site.dogether.member.entity.Member;
import site.dogether.member.repository.MemberRepository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Transactional
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ChallengeGroupServiceTest {

    @Autowired private ChallengeGroupService challengeGroupService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ChallengeGroupRepository challengeGroupRepository;
    @Autowired private ChallengeGroupMemberRepository challengeGroupMemberRepository;

    @Test
    void 챌린지_그룹을_생성한다() {
        //given
        CreateChallengeGroupApiRequestV1 request = new CreateChallengeGroupApiRequestV1(
                "운동 같이 하자",
                10,
                "TODAY",
                7
        );
        Member member = memberRepository.save(Member.create("providerId", "폰트"));

        //when
        String joinCode = challengeGroupService.createChallengeGroup(request, member.getId());

        //then
        assertThat(joinCode).isNotNull();
    }

    @Test
    void 참여중인_그룹이_5개_이상인_경우_챌린지_그룹을_생성하면_예외가_발생한다() {
        //given
        CreateChallengeGroupApiRequestV1 request = new CreateChallengeGroupApiRequestV1(
                "운동 같이 하자",
                10,
                "TODAY",
                7
        );
        Member member = memberRepository.save(Member.create("providerId", "폰트"));
        for (int i = 0; i < 5; i++) {
            challengeGroupService.createChallengeGroup(request, member.getId());
        }

        //when & then
        assertThatThrownBy(() -> challengeGroupService.createChallengeGroup(request, member.getId()))
                .isInstanceOf(JoiningChallengeGroupMaxCountException.class);
    }

    @Test
    void 챌린지_그룹에_참여한다() {
        //given
        Member member1 = memberRepository.save(Member.create("providerId1", "폰트"));
        Member member2 = memberRepository.save(Member.create("providerId2", "켈리"));
        ChallengeGroup challengeGroup = challengeGroupRepository.save(ChallengeGroupFixture.create());
        challengeGroupMemberRepository.save(new ChallengeGroupMember(challengeGroup, member1));

        //when
        JoinChallengeGroupDto joinChallengeGroupDto = challengeGroupService.joinChallengeGroup(
                challengeGroup.getJoinCode().getValue(), member2.getId()
        );

        //then
        assertThat(joinChallengeGroupDto).isNotNull();
        assertThat(joinChallengeGroupDto.groupName()).isEqualTo(challengeGroup.getName());
    }

    @Test
    void 존재하지_않는_그룹에_참가하면_예외가_발생한다() {
        //given
        Member member = memberRepository.save(Member.create("providerId", "폰트"));
        String joinCode = "invalidJoinCode";

        //when & then
        assertThatThrownBy(() -> challengeGroupService.joinChallengeGroup(joinCode, member.getId()))
                .isInstanceOf(JoiningChallengeGroupNotFoundException.class);
    }

    @Test
    void 이미_참여중인_그룹에_참여하면_예외가_발생한다() {
        //given
        Member member = memberRepository.save(Member.create("providerId", "폰트"));
        ChallengeGroup challengeGroup = challengeGroupRepository.save(ChallengeGroupFixture.create());
        challengeGroupMemberRepository.save(new ChallengeGroupMember(challengeGroup, member));

        //when & then
        assertThatThrownBy(() -> challengeGroupService.joinChallengeGroup(
                challengeGroup.getJoinCode().getValue(), member.getId()
        )).isInstanceOf(AlreadyJoinChallengeGroupException.class);
    }

    @Test
    void 정원이_초과된_그룹에_참여하면_예외가_발생한다() {
        //given
        Member member1 = memberRepository.save(Member.create("providerId1", "폰트"));
        Member member2 = memberRepository.save(Member.create("providerId2", "켈리"));
        Member member3 = memberRepository.save(Member.create("providerId3", "서은"));
        ChallengeGroup challengeGroup = challengeGroupRepository.save(ChallengeGroupFixture.create(2));
        challengeGroupMemberRepository.save(new ChallengeGroupMember(challengeGroup, member1));
        challengeGroupMemberRepository.save(new ChallengeGroupMember(challengeGroup, member2));

        //when & then
        assertThatThrownBy(() -> challengeGroupService.joinChallengeGroup(
                challengeGroup.getJoinCode().getValue(), member3.getId()))
                .isInstanceOf(JoiningChallengeGroupAlreadyFullMemberException.class);
    }

    @Test
    void 참여중인_챌린지_그룹을_모두_조회한다() {
        //given
        Member member = memberRepository.save(Member.create("providerId", "폰트"));
        ChallengeGroup challengeGroup1 = challengeGroupRepository.save(ChallengeGroupFixture.create());
        ChallengeGroup challengeGroup2 = challengeGroupRepository.save(ChallengeGroupFixture.create());
        challengeGroupMemberRepository.save(new ChallengeGroupMember(challengeGroup1, member));
        challengeGroupMemberRepository.save(new ChallengeGroupMember(challengeGroup2, member));

        //when
        JoiningChallengeGroupsWithLastSelectedGroupIndexDto result =
                challengeGroupService.getJoiningChallengeGroups(member.getId());

        //then
        assertThat(result.joiningChallengeGroups().size()).isEqualTo(2);
        assertThat(result.lastSelectedGroupIndex()).isEqualTo(0);
        assertThat(result.joiningChallengeGroups().get(0).groupName()).isEqualTo(challengeGroup1.getName());
        assertThat(result.joiningChallengeGroups().get(1).groupName()).isEqualTo(challengeGroup2.getName());
    }

    @Test
    void 챌린지_그룹을_탈퇴한다() {
        //given
        Member member1 = memberRepository.save(Member.create("providerId1", "폰트"));
        ChallengeGroup challengeGroup = challengeGroupRepository.save(ChallengeGroupFixture.create());
        ChallengeGroupMember challengeGroupMember = challengeGroupMemberRepository.save(
                new ChallengeGroupMember(challengeGroup, member1));

        //when
        challengeGroupService.leaveChallengeGroup(member1.getId(), challengeGroup.getId());

        //then
        assertThat(challengeGroupMemberRepository.findById(challengeGroupMember.getId()))
                .isEmpty();
    }

    @Test
    void 존재하지_않는_챌린지_그룹을_탈퇴하면_예외가_발생한다() {
        //given
        Member member = memberRepository.save(Member.create("providerId1", "폰트"));

        //when & then
        assertThatThrownBy(() -> challengeGroupService.leaveChallengeGroup(member.getId(), 1L))
                .isInstanceOf(ChallengeGroupNotFoundException.class);
    }

    @Test
    void 속해있지_않은_챌린지_그룹을_탈퇴하면_예외가_발생한다() {
        //given
        Member member1 = memberRepository.save(Member.create("providerId1", "폰트"));
        ChallengeGroup challengeGroup = challengeGroupRepository.save(ChallengeGroupFixture.create());

        //when & then
        assertThatThrownBy(() -> challengeGroupService.leaveChallengeGroup(member1.getId(), challengeGroup.getId()))
                .isInstanceOf(MemberNotInChallengeGroupException.class);
    }

    @Test
    void 참여중인_그룹이_있는지_조회한다__있는_경우() {
        //given
        Member member = memberRepository.save(Member.create("providerId", "폰트"));
        ChallengeGroup challengeGroup = challengeGroupRepository.save(ChallengeGroupFixture.create());
        challengeGroupMemberRepository.save(new ChallengeGroupMember(challengeGroup, member));

        //when
        boolean isParticipating = challengeGroupService.isChallengeGroupParticipationRequired(member.getId()).checkParticipating();

        //then
        assertThat(isParticipating).isFalse();
    }

    @Test
    void 참여중인_그룹이_없는지_조회한다__없는_경우() {
        //given
        Member member = memberRepository.save(Member.create("providerId", "폰트"));

        //when
        boolean isParticipating = challengeGroupService.isChallengeGroupParticipationRequired(member.getId()).checkParticipating();

        //then
        assertThat(isParticipating).isTrue();
    }
}
