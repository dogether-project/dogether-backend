package site.dogether.challengegroup.service.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupStatus;
import site.dogether.challengegroup.fixture.ChallengeGroupFixture;
import site.dogether.challengegroup.repository.ChallengeGroupRepository;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class ChallengeGroupStatusUpdateServiceTest {

    @Autowired
    private ChallengeGroupStatusUpdateService challengeGroupStatusUpdateService;

    @Autowired
    private ChallengeGroupRepository challengeGroupRepository;

    @DisplayName("FINISHED가 아닌 모든 챌린지 그룹의 상태를 업데이트한다")
    @Test
    void updateChallengeGroupStatus() {
        // Given
        final ChallengeGroup readyGroup = challengeGroupRepository.save(
                ChallengeGroupFixture.create(ChallengeGroupStatus.READY)
        );
        final ChallengeGroup runningGroup = challengeGroupRepository.save(
                ChallengeGroupFixture.create(ChallengeGroupStatus.RUNNING)
        );
        final ChallengeGroup dDayGroup = challengeGroupRepository.save(
                ChallengeGroupFixture.create(ChallengeGroupStatus.D_DAY)
        );
        final ChallengeGroup finishedGroup = challengeGroupRepository.save(
                ChallengeGroupFixture.create(ChallengeGroupStatus.FINISHED)
        );

        // When
        challengeGroupStatusUpdateService.updateChallengeGroupStatus();

        // Then
        final ChallengeGroup updatedReadyGroup = challengeGroupRepository.findById(readyGroup.getId()).orElseThrow();
        final ChallengeGroup updatedRunningGroup = challengeGroupRepository.findById(runningGroup.getId()).orElseThrow();
        final ChallengeGroup updatedDDayGroup = challengeGroupRepository.findById(dDayGroup.getId()).orElseThrow();
        final ChallengeGroup updatedFinishedGroup = challengeGroupRepository.findById(finishedGroup.getId()).orElseThrow();

        // READY 그룹은 시작 전이므로 READY 유지
        assertThat(updatedReadyGroup.getStatus()).isEqualTo(ChallengeGroupStatus.READY);

        // RUNNING 그룹은 진행 중이므로 RUNNING 유지
        assertThat(updatedRunningGroup.getStatus()).isEqualTo(ChallengeGroupStatus.RUNNING);

        // D_DAY 그룹은 마지막 날이므로 D_DAY 유지
        assertThat(updatedDDayGroup.getStatus()).isEqualTo(ChallengeGroupStatus.D_DAY);

        // FINISHED 그룹은 업데이트되지 않아야 하므로 FINISHED 유지
        assertThat(updatedFinishedGroup.getStatus()).isEqualTo(ChallengeGroupStatus.FINISHED);
    }

    @DisplayName("시작일이 오늘인 READY 그룹은 RUNNING으로 업데이트된다")
    @Test
    void updateReadyToRunning() {
        // Given
        final ChallengeGroup readyGroup = challengeGroupRepository.save(
                ChallengeGroupFixture.create("테스트 그룹", 10, LocalDate.now(), LocalDate.now().plusDays(7))
        );

        // When
        challengeGroupStatusUpdateService.updateChallengeGroupStatus();

        // Then
        final ChallengeGroup updatedGroup = challengeGroupRepository.findById(readyGroup.getId()).orElseThrow();
        assertThat(updatedGroup.getStatus()).isEqualTo(ChallengeGroupStatus.RUNNING);
    }

    @DisplayName("종료일이 오늘인 RUNNING 그룹은 D_DAY로 업데이트된다")
    @Test
    void updateRunningToDDay() {
        // Given
        final LocalDate endAt = LocalDate.now();
        final ChallengeGroup runningGroup = challengeGroupRepository.save(
                ChallengeGroupFixture.create("테스트 그룹", 10, endAt.minusDays(7), endAt)
        );

        // When
        challengeGroupStatusUpdateService.updateChallengeGroupStatus();

        // Then
        final ChallengeGroup updatedGroup = challengeGroupRepository.findById(runningGroup.getId()).orElseThrow();
        assertThat(updatedGroup.getStatus()).isEqualTo(ChallengeGroupStatus.D_DAY);
    }

    @DisplayName("종료일이 지난 D_DAY 그룹은 FINISHED로 업데이트된다")
    @Test
    void updateDDayToFinished() {
        // Given
        final LocalDate endAt = LocalDate.now().minusDays(1);
        final ChallengeGroup dDayGroup = challengeGroupRepository.save(
                ChallengeGroupFixture.create("테스트 그룹", 10, endAt.minusDays(7), endAt)
        );

        // When
        challengeGroupStatusUpdateService.updateChallengeGroupStatus();

        // Then
        final ChallengeGroup updatedGroup = challengeGroupRepository.findById(dDayGroup.getId()).orElseThrow();
        assertThat(updatedGroup.getStatus()).isEqualTo(ChallengeGroupStatus.FINISHED);
    }
}
