package site.dogether.notification.config.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    private static final String FIREBASE_KEY_FILE_DIR = "/firebase/";
    private static final String FIREBASE_KEY_FILE_BASE_NAME = "dogether-firebase-key.json";

    @PostConstruct
    public void init() throws IOException {
        try {
            final InputStream serviceAccount = new ClassPathResource(FIREBASE_KEY_FILE_DIR + FIREBASE_KEY_FILE_BASE_NAME).getInputStream();
            final FirebaseOptions options = FirebaseOptions.builder()
                                                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                                                .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            log.info("FirebaseApp 초기화 성공 - {}", FIREBASE_KEY_FILE_DIR + FIREBASE_KEY_FILE_BASE_NAME);
        } catch (final Exception e) {
            log.error("FirebaseApp 초기화 실패", e);
            throw e;
        }
    }
}
