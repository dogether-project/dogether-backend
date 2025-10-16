
package site.dogether.challengegroup.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupMember;
import site.dogether.challengegroup.entity.ChallengeGroupStatus;
import site.dogether.challengegroup.exception.*;
import site.dogether.challengegroup.fixture.ChallengeGroupFixture;
import site.dogether.challengegroup.repository.ChallengeGroupMemberRepository;
import site.dogether.challengegroup.repository.ChallengeGroupRepository;
import site.dogether.member.entity.Member;
import site.dogether.member.repository.MemberRepository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ChallengeGroupPolicyTest {

    @Autowired
    private ChallengeGroupPolicy challengeGroupPolicy;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ChallengeGroupRepository challengeGroupRepository;

    @Autowired
    private ChallengeGroupMemberRepository challengeGroupMemberRepository;

    @DisplayName("참여 중인 그룹이 최대치(5개) 미만일 경우 예외가 발생하지 않는다.")
    @Test
    void validateChallengeGroupMaxCount_Success() {
        // given
        Member member = Member.create("testuser", "testuser");
        memberRepository.save(member);

        for (int i = 0; i < 4; i++) {
            ChallengeGroup group = challengeGroupRepository.save(ChallengeGroupFixture.create("group" + i));
            challengeGroupMemberRepository.save(new ChallengeGroupMember(group, member));
        }

        // when & then
        assertDoesNotThrow(() -> challengeGroupPolicy.validateChallengeGroupMaxCount(member));
    }

    @DisplayName("참여 중인 그룹이 최대치(5개) 이상일 경우 예외가 발생한다.")
    @Test
    void validateChallengeGroupMaxCount_Fail() {
        // given
        Member member = Member.create("testuser", "testuser");
        memberRepository.save(member);

        for (int i = 0; i < 5; i++) {
            ChallengeGroup group = challengeGroupRepository.save(ChallengeGroupFixture.create("group" + i));
            challengeGroupMemberRepository.save(new ChallengeGroupMember(group, member));
        }

        // when & then
        assertThrows(JoiningChallengeGroupMaxCountException.class,
            () -> challengeGroupPolicy.validateChallengeGroupMaxCount(member));
    }

    @DisplayName("종료되지 않은 그룹에 참여 시 예외가 발생하지 않는다.")
    @Test
    void validateChallengeGroupNotFinished_Success() {
        // given
        ChallengeGroup group = challengeGroupRepository.save(ChallengeGroupFixture.create(ChallengeGroupStatus.RUNNING));

        // when & then
        assertDoesNotThrow(() -> challengeGroupPolicy.validateChallengeGroupNotFinished(group));
    }

    @DisplayName("종료된 그룹에 참여 시 예외가 발생한다.")
    @Test
    void validateChallengeGroupNotFinished_Fail() {
        // given
        ChallengeGroup group = challengeGroupRepository.save(ChallengeGroupFixture.create(ChallengeGroupStatus.FINISHED));

        // when & then
        assertThrows(FinishedChallengeGroupException.class,
            () -> challengeGroupPolicy.validateChallengeGroupNotFinished(group));
    }

    @DisplayName("정원이 가득 차지 않은 그룹에 참여 시 예외가 발생하지 않는다.")
    @Test
    void validateChallengeGroupHasMaximumMember_Success() {
        // given
        ChallengeGroup group = challengeGroupRepository.save(ChallengeGroupFixture.create(2));
        Member member1 = memberRepository.save(Member.create("user1", "user1"));
        challengeGroupMemberRepository.save(new ChallengeGroupMember(group, member1));

        // when & then
        assertDoesNotThrow(() -> challengeGroupPolicy.validateChallengeGroupHasMaximumMember(group));
    }

    @DisplayName("정원이 가득 찬 그룹에 참여 시 예외가 발생한다.")
    @Test
    void validateChallengeGroupHasMaximumMember_Fail() {
        // given
        ChallengeGroup group = challengeGroupRepository.save(ChallengeGroupFixture.create(2));
        Member member1 = memberRepository.save(Member.create("user1", "user1"));
        Member member2 = memberRepository.save(Member.create("user2", "user2"));
        challengeGroupMemberRepository.save(new ChallengeGroupMember(group, member1));
        challengeGroupMemberRepository.save(new ChallengeGroupMember(group, member2));

        // when & then
        assertThrows(JoiningChallengeGroupAlreadyFullMemberException.class,
            () -> challengeGroupPolicy.validateChallengeGroupHasMaximumMember(group));
    }

    @DisplayName("참여하지 않은 그룹일 경우 예외가 발생하지 않는다.")
    @Test
    void validateMemberInSameChallengeGroup_Success() {
        // given
        Member member = memberRepository.save(Member.create("user1", "user1"));
        ChallengeGroup group = challengeGroupRepository.save(ChallengeGroupFixture.create());

        // when & then
        assertDoesNotThrow(() -> challengeGroupPolicy.validateMemberInSameChallengeGroup(group, member));
    }

    @DisplayName("이미 참여한 그룹일 경우 예외가 발생한다.")
    @Test
    void validateMemberInSameChallengeGroup_Fail() {
        // given
        Member member = memberRepository.save(Member.create("user1", "user1"));
        ChallengeGroup group = challengeGroupRepository.save(ChallengeGroupFixture.create());
        challengeGroupMemberRepository.save(new ChallengeGroupMember(group, member));

        // when & then
        assertThrows(AlreadyJoinChallengeGroupException.class,
            () -> challengeGroupPolicy.validateMemberInSameChallengeGroup(group, member));
    }

    @DisplayName("진행중인 그룹일 경우 예외가 발생하지 않는다.")
    @Test
    void validateChallengeGroupIsRunning_Success() {
        // given
        ChallengeGroup group = challengeGroupRepository.save(ChallengeGroupFixture.create(ChallengeGroupStatus.RUNNING));

        // when & then
        assertDoesNotThrow(() -> challengeGroupPolicy.validateChallengeGroupIsRunning(group));
    }

    @DisplayName("시작 전인 그룹일 경우 예외가 발생한다.")
    @Test
    void validateChallengeGroupIsRunning_Fail_Recruiting() {
        // given
        ChallengeGroup group = challengeGroupRepository.save(ChallengeGroupFixture.create(ChallengeGroupStatus.READY));

        // when & then
        assertThrows(NotRunningChallengeGroupException.class,
            () -> challengeGroupPolicy.validateChallengeGroupIsRunning(group));
    }

    @DisplayName("멤버가 그룹에 속해있을 경우 예외가 발생하지 않는다.")
    @Test
    void validateMemberIsInChallengeGroup_Success() {
        // given
        Member member = memberRepository.save(Member.create("user1", "user1"));
        ChallengeGroup group = challengeGroupRepository.save(ChallengeGroupFixture.create());
        challengeGroupMemberRepository.save(new ChallengeGroupMember(group, member));

        // when & then
        assertDoesNotThrow(() -> challengeGroupPolicy.validateMemberIsInChallengeGroup(group, member));
    }

    @DisplayName("멤버가 그룹에 속해있지 않을 경우 예외가 발생한다.")
    @Test
    void validateMemberIsInChallengeGroup_Fail() {
        // given
        Member member = memberRepository.save(Member.create("user1", "user1"));
        ChallengeGroup group = challengeGroupRepository.save(ChallengeGroupFixture.create());

        // when & then
        assertThrows(MemberNotInChallengeGroupException.class,
            () -> challengeGroupPolicy.validateMemberIsInChallengeGroup(group, member));
    }
}
