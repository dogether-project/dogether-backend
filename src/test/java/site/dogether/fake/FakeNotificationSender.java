package site.dogether.fake;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import site.dogether.notification.sender.NotificationRequest;
import site.dogether.notification.sender.NotificationSender;

@Slf4j
@Profile("test")
@Primary
@Component
public class FakeNotificationSender implements NotificationSender {

    @Override
    public void send(final NotificationRequest request) {
        log.info("Mock 푸시 알림 전송!!");
    }
}
