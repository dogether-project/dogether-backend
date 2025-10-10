package site.dogether.challengegroup.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupStatus;
import site.dogether.challengegroup.repository.ChallengeGroupRepository;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChallengeGroupStatusUpdateService {

    private final ChallengeGroupRepository challengeGroupRepository;

    @Transactional
    public void updateChallengeGroupStatus() {
        final List<ChallengeGroup> notFinishedGroups = challengeGroupRepository.findByStatusNot(ChallengeGroupStatus.FINISHED);

        for (final ChallengeGroup notFinishedGroup : notFinishedGroups) {
            notFinishedGroup.updateStatus();
            log.info("챌린지 그룹 상태 업데이트: groupId={}, status={}", notFinishedGroup.getId(), notFinishedGroup.getStatus());
        }
    }
}
