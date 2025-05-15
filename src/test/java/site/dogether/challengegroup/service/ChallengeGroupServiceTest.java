package site.dogether.challengegroup.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.controller.request.CreateChallengeGroupRequest;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupMember;
import site.dogether.challengegroup.exception.ChallengeGroupNotFoundException;
import site.dogether.challengegroup.exception.FullMemberInChallengeGroupException;
import site.dogether.challengegroup.exception.JoiningChallengeGroupMaxCountException;
import site.dogether.challengegroup.exception.MemberAlreadyInChallengeGroupException;
import site.dogether.challengegroup.exception.MemberNotInChallengeGroupException;
import site.dogether.challengegroup.repository.ChallengeGroupMemberRepository;
import site.dogether.challengegroup.repository.ChallengeGroupRepository;
import site.dogether.challengegroup.service.dto.JoinChallengeGroupDto;
import site.dogether.member.entity.Member;
import site.dogether.member.repository.MemberRepository;

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
        CreateChallengeGroupRequest request = new CreateChallengeGroupRequest(
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
        CreateChallengeGroupRequest request = new CreateChallengeGroupRequest(
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
        CreateChallengeGroupRequest request = new CreateChallengeGroupRequest(
                "운동 같이 하자",
                10,
                "TODAY",
                7
        );
        Member member1 = memberRepository.save(Member.create("providerId1", "폰트"));
        Member member2 = memberRepository.save(Member.create("providerId2", "켈리"));
        String joinCode = challengeGroupService.createChallengeGroup(request, member1.getId());

        //when
        JoinChallengeGroupDto joinChallengeGroupDto = challengeGroupService.joinChallengeGroup(joinCode, member2.getId());

        //then
        assertThat(joinChallengeGroupDto.groupName()).isEqualTo("운동 같이 하자");
        assertThat(joinChallengeGroupDto.duration()).isEqualTo(7);
        assertThat(joinChallengeGroupDto.maximumMemberCount()).isEqualTo(10);
        assertThat(joinChallengeGroupDto.startAt())
                .isEqualTo(LocalDate.now().format(DateTimeFormatter.ofPattern("yy.MM.dd")));
        assertThat(joinChallengeGroupDto.endAt())
                .isEqualTo(LocalDate.now().plusDays(7).format(DateTimeFormatter.ofPattern("yy.MM.dd")));
    }

    @Test
    void 존재하지_않는_그룹에_참가하면_예외가_발생한다() {
        //given
        Member member = memberRepository.save(Member.create("providerId", "폰트"));
        String joinCode = "invalidJoinCode";

        //when & then
        assertThatThrownBy(() -> challengeGroupService.joinChallengeGroup(joinCode, member.getId()))
                .isInstanceOf(ChallengeGroupNotFoundException.class);
    }

    @Test
    void 이미_참여중인_그룹에_참여하면_예외가_발생한다() {
        //given
        CreateChallengeGroupRequest request = new CreateChallengeGroupRequest(
                "운동 같이 하자",
                10,
                "TODAY",
                7
        );
        Member member = memberRepository.save(Member.create("providerId", "폰트"));
        String joinCode = challengeGroupService.createChallengeGroup(request, member.getId());

        //when & then
        assertThatThrownBy(() -> challengeGroupService.joinChallengeGroup(joinCode, member.getId()))
                .isInstanceOf(MemberAlreadyInChallengeGroupException.class);
    }

    @Test
    void 정원이_초과된_그룹에_참여하면_예외가_발생한다() {
        //given
        int maximumMemberCount = 2;
        CreateChallengeGroupRequest request = new CreateChallengeGroupRequest(
                "운동 같이 하자",
                maximumMemberCount,
                "TODAY",
                7
        );
        Member member1 = memberRepository.save(Member.create("providerId1", "폰트"));
        String joinCode = challengeGroupService.createChallengeGroup(request, member1.getId());
        Member member2 = memberRepository.save(Member.create("providerId2", "켈리"));
        challengeGroupService.joinChallengeGroup(joinCode, member2.getId());
        Member member3 = memberRepository.save(Member.create("providerId3", "서은"));

        //when & then
        assertThatThrownBy(() -> challengeGroupService.joinChallengeGroup(joinCode, member3.getId()))
                .isInstanceOf(FullMemberInChallengeGroupException.class);
    }

    @Test
    void 참여중인_챌린지_그룹을_모두_조회한다() {
        //given
        CreateChallengeGroupRequest request1 = new CreateChallengeGroupRequest(
                "운동 같이 하자",
                10,
                "TODAY",
                7
        );
        CreateChallengeGroupRequest request2 = new CreateChallengeGroupRequest(
                "공부 같이 하자",
                11,
                "TOMORROW",
                14
        );
        Member member1 = memberRepository.save(Member.create("providerId", "폰트"));
        Member member2 = memberRepository.save(Member.create("providerId2", "켈리"));
        String joinCode1 = challengeGroupService.createChallengeGroup(request1, member1.getId());
        String joinCode2 = challengeGroupService.createChallengeGroup(request2, member1.getId());

        //when
        JoinChallengeGroupDto joinChallengeGroupDto1 = challengeGroupService.joinChallengeGroup(joinCode1, member2.getId());
        JoinChallengeGroupDto joinChallengeGroupDto2 = challengeGroupService.joinChallengeGroup(joinCode2, member2.getId());

        //then
        assertThat(joinChallengeGroupDto1.groupName()).isEqualTo("운동 같이 하자");
        assertThat(joinChallengeGroupDto1.duration()).isEqualTo(7);
        assertThat(joinChallengeGroupDto1.maximumMemberCount()).isEqualTo(10);
        assertThat(joinChallengeGroupDto1.startAt())
                .isEqualTo(LocalDate.now().format(DateTimeFormatter.ofPattern("yy.MM.dd")));
        assertThat(joinChallengeGroupDto1.endAt())
                .isEqualTo(LocalDate.now().plusDays(7).format(DateTimeFormatter.ofPattern("yy.MM.dd")));

        assertThat(joinChallengeGroupDto2.groupName()).isEqualTo("공부 같이 하자");
        assertThat(joinChallengeGroupDto2.duration()).isEqualTo(14);
        assertThat(joinChallengeGroupDto2.maximumMemberCount()).isEqualTo(11);
        assertThat(joinChallengeGroupDto2.startAt())
                .isEqualTo(LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yy.MM.dd")));
        assertThat(joinChallengeGroupDto2.endAt())
                .isEqualTo(LocalDate.now().plusDays(15).format(DateTimeFormatter.ofPattern("yy.MM.dd")));
    }

    @Test
    void 챌린지_그룹을_탈퇴한다() {
        //given
        Member member1 = memberRepository.save(Member.create("providerId1", "폰트"));
        ChallengeGroup challengeGroup = challengeGroupRepository.save(ChallengeGroup.create(
                "운동 같이 하자",
                10,
                LocalDate.now(),
                LocalDate.now().plusDays(7)
        ));
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
        ChallengeGroup challengeGroup = challengeGroupRepository.save(ChallengeGroup.create(
                "운동 같이 하자",
                10,
                LocalDate.now(),
                LocalDate.now().plusDays(7)
        ));

        //when & then
        assertThatThrownBy(() -> challengeGroupService.leaveChallengeGroup(member1.getId(), challengeGroup.getId()))
                .isInstanceOf(MemberNotInChallengeGroupException.class);
    }

    @Test
    void 참여중인_그룹이_있는지_조회한다__있는_경우() {
        //given
        CreateChallengeGroupRequest request = new CreateChallengeGroupRequest(
                "운동 같이 하자",
                10,
                "TODAY",
                7
        );
        Member member = memberRepository.save(Member.create("providerId", "폰트"));
        challengeGroupService.createChallengeGroup(request, member.getId());

        //when
        boolean isParticipating = challengeGroupService.isParticipatingChallengeGroup(member.getId()).isParticipating();

        //then
        assertThat(isParticipating).isTrue();
    }

    @Test
    void 참여중인_그룹이_없는지_조회한다__없는_경우() {
        //given
        Member member = memberRepository.save(Member.create("providerId", "폰트"));

        //when
        boolean isParticipating = challengeGroupService.isParticipatingChallengeGroup(member.getId()).isParticipating();

        //then
        assertThat(isParticipating).isFalse();
    }
}
