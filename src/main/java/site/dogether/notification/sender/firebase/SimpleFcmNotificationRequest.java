package site.dogether.notification.sender.firebase;

import com.google.firebase.messaging.*;

public class SimpleFcmNotificationRequest extends FcmNotificationRequest {

    private final String title;
    private final String body;
    private final String type;

    public SimpleFcmNotificationRequest(
        final String fcmToken,
        final String title,
        final String body,
        final String type
    ) {
        super(fcmToken);
        this.title = title;
        this.body = body;
        this.type = type;
    }

    @Override
    public Message convertFcmMessage() {
        return Message.builder()
            .setNotification(createNotification())
            .setToken(fcmToken)
            .setApnsConfig(createApnsConfig())
            .setAndroidConfig(createAndroidConfig())
            .putData("type", type)
            .build();
    }

    private ApnsConfig createApnsConfig() {
        return ApnsConfig.builder()
            .setAps(
                Aps.builder()
                    .setContentAvailable(true)
                    .build())
            .build();
    }

    private static AndroidConfig createAndroidConfig() {
        return AndroidConfig.builder()
            .setPriority(AndroidConfig.Priority.HIGH)
            .build();
    }

    private Notification createNotification() {
        return Notification.builder()
            .setTitle(title)
            .setBody(body)
            .build();
    }
}
