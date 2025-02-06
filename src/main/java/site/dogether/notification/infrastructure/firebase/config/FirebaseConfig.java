package site.dogether.notification.infrastructure.firebase.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    private static final String FIREBASE_KEY_FILE_DIR = "/firebase/";
    private static final String FIREBASE_KEY_FILE_EXTENSION = ".json";
    private static final String FIREBASE_KEY_FILE_BASE_NAME = "dogether-firebase-key-";

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @PostConstruct
    public void init() throws IOException {
        try {
            final InputStream serviceAccount = new ClassPathResource(getFirebaseKeyFileName()).getInputStream();
            final FirebaseOptions options = FirebaseOptions.builder()
                                                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                                                .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            log.info("FirebaseApp 초기화 성공 - {}", getFirebaseKeyFileName());
        } catch (final Exception e) {
            log.error("FirebaseApp 초기화 실패", e);
            throw e;
        }
    }

    private String getFirebaseKeyFileName() {
        if (!activeProfile.equals("dev") && !activeProfile.equals("prod")) {
            activeProfile = "dev";
        }

        return FIREBASE_KEY_FILE_DIR + FIREBASE_KEY_FILE_BASE_NAME + activeProfile + FIREBASE_KEY_FILE_EXTENSION;
    }
}
