package site.dogether.memberactivity.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupMember;
import site.dogether.challengegroup.entity.JoinCode;
import site.dogether.challengegroup.repository.ChallengeGroupMemberRepository;
import site.dogether.challengegroup.repository.ChallengeGroupRepository;
import site.dogether.member.entity.Member;
import site.dogether.member.repository.MemberRepository;
import site.dogether.memberactivity.entity.DailyTodoStats;
import site.dogether.memberactivity.repository.DailyTodoStatsRepository;
import site.dogether.memberactivity.service.dto.CertificationPeriodDto;
import site.dogether.memberactivity.service.dto.ChallengeGroupInfoDto;
import site.dogether.memberactivity.service.dto.MyCertificationStatsInChallengeGroupDto;
import site.dogether.memberactivity.service.dto.MyRankInChallengeGroupDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class MemberActivityServiceTest {

    @Autowired
    private MemberActivityService memberActivityService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private DailyTodoStatsRepository dailyTodoStatsRepository;

    @Autowired
    private ChallengeGroupRepository challengeGroupRepository;

    @Autowired
    private ChallengeGroupMemberRepository challengeGroupMemberRepository;

    private static Member createMember(final String name) {
        return new Member(
            null,
            "provider_id " + name,
            name,
            "profile_image_url " + name,
            LocalDateTime.now()
        );
    }

    private static ChallengeGroup createChallengeGroup(final String name) {
        return new ChallengeGroup(
            name,
            5,
            LocalDate.now(),
            LocalDate.now().plusWeeks(4),
            JoinCode.generate(),
            LocalDateTime.now()
        );
    }

    @DisplayName("유효한 값(member 객체)이 들어오면 사용자별 데일리 투두 통계 테이블을 초기화하고 DB에 반영 요청 한다.")
    @Test
    void givenMember_whenInitDailyTodoStats_thenInitSuccess() {
        //Given
        final Member member = createMember("member1");
        memberRepository.save(member);

        //When
        memberActivityService.initDailyTodoStats(member);

        //Then
        final DailyTodoStats dailyTodoStats = dailyTodoStatsRepository.findByMember(member)
            .orElseThrow();
        final int expectedCertificatedCount = 0;
        final int expectedApprovedCount = 0;
        final int expectedRejectedCount = 0;

        assertThat(dailyTodoStats.getCertificatedCount()).isEqualTo(expectedCertificatedCount);
        assertThat(dailyTodoStats.getApprovedCount()).isEqualTo(expectedApprovedCount);
        assertThat(dailyTodoStats.getRejectedCount()).isEqualTo(expectedRejectedCount);
    }

    @DisplayName("유효한 값(memberId, groupId)이 들어오면 해당 그룹의 정보를 반환한다.")
    @Test
    void givenMemberIdAndGroupId_whenGetChallengeGroupInfo_thenReturnChallengeGroupInfo() {
        //Given
        final Member member = createMember("member1");
        memberRepository.save(member);

        final ChallengeGroup challengeGroup = createChallengeGroup("challengeGroup1");
        challengeGroupRepository.save(challengeGroup);

        final ChallengeGroupMember challengeGroupMember = new ChallengeGroupMember(challengeGroup, member);
        challengeGroupMemberRepository.save(challengeGroupMember);

        //When
        final ChallengeGroupInfoDto challengeGroupInfo = memberActivityService.getChallengeGroupInfo(member.getId(), challengeGroup.getId());

        //Then
        final ChallengeGroupInfoDto expectedChallengeGroupInfo = new ChallengeGroupInfoDto(
            challengeGroup.getName(),
            challengeGroup.getMaximumMemberCount(),
            1,
            challengeGroup.getJoinCode().getValue(),
            challengeGroup.getEndAt().format(DateTimeFormatter.ofPattern("yy.MM.dd"))
        );

        assertThat(challengeGroupInfo).isEqualTo(expectedChallengeGroupInfo);
    }

    @DisplayName("유효한 값(memberId, groupId)이 들어오면 해당 그룹 내 사용자의 기간 별 인증 정보를 반환한다.")
    @Test
    void givenMemberIdAndGroupId_whenGetCertificationPeriods_thenReturnCertificationPeriods() {
        //Given
        final Member member = createMember("member1");
        memberRepository.save(member);

        final ChallengeGroup challengeGroup = createChallengeGroup("challengeGroup1");
        challengeGroupRepository.save(challengeGroup);

        final ChallengeGroupMember challengeGroupMember = new ChallengeGroupMember(challengeGroup, member);
        challengeGroupMemberRepository.save(challengeGroupMember);

        //When
        final List<CertificationPeriodDto> certificationPeriods = memberActivityService.getCertificationPeriods(member.getId(), challengeGroup.getId());

        //Then
        final List<CertificationPeriodDto> expectedCertificationPeriods = List.of(
            new CertificationPeriodDto(
                1,
                0,
                0,
                0
            )
        );

        assertThat(certificationPeriods).isEqualTo(expectedCertificationPeriods);
    }

    @DisplayName("유효한 값(memberId, groupId)이 들어오면 해당 그룹의 사용자 랭킹을 반환한다.")
    @Test
    void givenMemberIdAndGroupId_whenGetMyRankInChallengeGroup_thenReturnMyRank() {
        //Given
        final Member member = createMember("member1");
        memberRepository.save(member);

        final ChallengeGroup challengeGroup = createChallengeGroup("challengeGroup1");
        challengeGroupRepository.save(challengeGroup);

        final ChallengeGroupMember challengeGroupMember = new ChallengeGroupMember(challengeGroup, member);
        challengeGroupMemberRepository.save(challengeGroupMember);

        //When
        final MyRankInChallengeGroupDto myRank = memberActivityService.getMyRankInChallengeGroup(member.getId(), challengeGroup.getId());

        //Then
        final MyRankInChallengeGroupDto expectedMyRank = new MyRankInChallengeGroupDto(1, 1);

        assertThat(myRank).isEqualTo(expectedMyRank);
    }

    @DisplayName("유효한 값(memberId, groupId)이 들어오면 해당 그룹의 사용자 투두 인증 통계를 반환한다.")
    @Test
    void givenMemberIdAndGroupId_whenGetMyCertificationStatsInChallengeGroup_thenReturnMyCertificationStats() {
        //Given
        final Member member = createMember("member1");
        memberRepository.save(member);

        final ChallengeGroup challengeGroup = createChallengeGroup("challengeGroup1");
        challengeGroupRepository.save(challengeGroup);

        final ChallengeGroupMember challengeGroupMember = new ChallengeGroupMember(challengeGroup, member);
        challengeGroupMemberRepository.save(challengeGroupMember);

        //When
        final MyCertificationStatsInChallengeGroupDto myCertificationStats = memberActivityService.getMyChallengeGroupStats(member.getId(), challengeGroup.getId());

        //Then
        final MyCertificationStatsInChallengeGroupDto expectedMyCertificationStats = new MyCertificationStatsInChallengeGroupDto(0, 0, 0);

        assertThat(myCertificationStats).isEqualTo(expectedMyCertificationStats);
    }
}