package site.dogether.challengegroup.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupStatus;
import site.dogether.challengegroup.exception.ChallengeGroupNotFoundException;
import site.dogether.challengegroup.exception.JoiningChallengeGroupNotFoundException;
import site.dogether.challengegroup.fixture.ChallengeGroupFixture;
import site.dogether.challengegroup.repository.ChallengeGroupRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
class ChallengeGroupReaderTest {

    @Autowired
    private ChallengeGroupReader challengeGroupReader;

    @Autowired
    private ChallengeGroupRepository challengeGroupRepository;

    @DisplayName("ID로 챌린지 그룹을 조회한다")
    @Test
    void getById() {
        // Given
        final ChallengeGroup savedGroup = challengeGroupRepository.save(ChallengeGroupFixture.create());

        // When
        final ChallengeGroup foundGroup = challengeGroupReader.getById(savedGroup.getId());

        // Then
        assertThat(foundGroup.getId()).isEqualTo(savedGroup.getId());
        assertThat(foundGroup.getName()).isEqualTo(savedGroup.getName());
    }

    @DisplayName("존재하지 않는 ID로 조회하면 예외가 발생한다")
    @Test
    void getByIdNotFound() {
        // Given
        final Long notExistingId = 99999L;

        // When & Then
        assertThatThrownBy(() -> challengeGroupReader.getById(notExistingId))
            .isInstanceOf(ChallengeGroupNotFoundException.class)
            .hasMessage(String.format("존재하지 않는 챌린지 그룹 id입니다. (%d)", notExistingId));
    }

    @DisplayName("참여 코드로 챌린지 그룹을 조회한다")
    @Test
    void getByJoinCode() {
        // Given
        final ChallengeGroup savedGroup = challengeGroupRepository.save(ChallengeGroupFixture.create());
        final String joinCode = savedGroup.getJoinCode().getValue();

        // When
        final ChallengeGroup foundGroup = challengeGroupReader.getByJoinCode(joinCode);

        // Then
        assertThat(foundGroup.getId()).isEqualTo(savedGroup.getId());
        assertThat(foundGroup.getJoinCode().getValue()).isEqualTo(joinCode);
    }

    @DisplayName("존재하지 않는 참여 코드로 조회하면 예외가 발생한다")
    @Test
    void getByJoinCodeNotFound() {
        // Given
        final String notExistingJoinCode = "INVALID_CODE";

        // When & Then
        assertThatThrownBy(() -> challengeGroupReader.getByJoinCode(notExistingJoinCode))
            .isInstanceOf(JoiningChallengeGroupNotFoundException.class)
            .hasMessage(String.format("참여하려는 챌린지 그룹이 존재하지 않습니다. (%s)", notExistingJoinCode));
    }

    @DisplayName("특정 상태가 아닌 챌린지 그룹들을 조회한다")
    @Test
    void findByStatusNot() {
        // Given
        challengeGroupRepository.save(ChallengeGroupFixture.create(ChallengeGroupStatus.READY));
        challengeGroupRepository.save(ChallengeGroupFixture.create(ChallengeGroupStatus.RUNNING));
        challengeGroupRepository.save(ChallengeGroupFixture.create(ChallengeGroupStatus.D_DAY));
        challengeGroupRepository.save(ChallengeGroupFixture.create(ChallengeGroupStatus.FINISHED));

        // When
        final List<ChallengeGroup> notFinishedGroups = challengeGroupReader.findByStatusNot(ChallengeGroupStatus.FINISHED);

        // Then
        assertThat(notFinishedGroups).hasSize(3);
        assertThat(notFinishedGroups)
            .allMatch(group -> group.getStatus() != ChallengeGroupStatus.FINISHED);
    }

    @DisplayName("특정 상태가 아닌 챌린지 그룹이 없으면 빈 리스트를 반환한다")
    @Test
    void findByStatusNotEmpty() {
        // Given
        challengeGroupRepository.save(ChallengeGroupFixture.create(ChallengeGroupStatus.FINISHED));

        // When
        final List<ChallengeGroup> notFinishedGroups = challengeGroupReader.findByStatusNot(ChallengeGroupStatus.FINISHED);

        // Then
        assertThat(notFinishedGroups).isEmpty();
    }
}
