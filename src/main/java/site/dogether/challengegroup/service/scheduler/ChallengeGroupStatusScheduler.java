package site.dogether.challengegroup.service.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import site.dogether.challengegroup.service.ChallengeGroupService;

@RequiredArgsConstructor
@Component
public class ChallengeGroupStatusScheduler {

    private final ChallengeGroupService challengeGroupService;

    @Scheduled(cron = "0 0 0 * * *")
    public void runStatusUpdate() {
        challengeGroupService.updateChallengeGroupStatus();
    }
}
