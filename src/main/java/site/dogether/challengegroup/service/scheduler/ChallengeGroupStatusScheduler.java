package site.dogether.challengegroup.service.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ChallengeGroupStatusScheduler {

    private final ChallengeGroupStatusUpdateService challengeGroupStatusUpdateService;

    @Scheduled(cron = "0 0 0 * * *")
    public void runStatusUpdate() {
        challengeGroupStatusUpdateService.updateChallengeGroupStatus();
    }
}
