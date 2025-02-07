package site.dogether.fake;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import site.dogether.notification.sender.NotificationRequest;
import site.dogether.notification.sender.NotificationSender;

@Profile("test")
@Primary
@Component
public class FakeNotificationSender implements NotificationSender {

    @Override
    public void send(final NotificationRequest request) {

    }
}
