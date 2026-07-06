package vn.vuavuive.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${app.firebase.database-url}")
    private String databaseUrl;

    @Value("${app.firebase.config-path}")
    private String configPath;

    @Value("${app.firebase.config-path}")
    private Resource firebaseConfigResource;

    @Bean
    public FirebaseApp firebaseApp() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                return FirebaseApp.getInstance();
            }

            log.info("Starting initialization of Firebase Admin SDK...");
            
            if (firebaseConfigResource == null || !firebaseConfigResource.exists()) {
                log.error("=========================================================================");
                log.error("CRITICAL ERROR: Firebase config file NOT FOUND at path: {}", configPath);
                log.error("Please download serviceAccountKey.json from Firebase Console and place it there.");
                log.error("=========================================================================");
                throw new IllegalStateException("Missing Firebase credential file: " + configPath);
            }

            GoogleCredentials credentials;
            try (InputStream serviceAccount = firebaseConfigResource.getInputStream()) {
                try {
                    credentials = GoogleCredentials.fromStream(serviceAccount);
                } catch (Exception e) {
                    log.error("Invalid Firebase credential file at path: {}", configPath, e);
                    throw new IllegalStateException("Invalid Firebase credential file: " + configPath, e);
                }
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setDatabaseUrl(databaseUrl)
                    .build();
            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info("Firebase Admin SDK initialized successfully for project: {}", app.getName());
            return app;
        } catch (IOException e) {
            log.error("Failed to read Firebase serviceAccountKey.json", e);
            throw new RuntimeException("Firebase credentials read failed", e);
        }
    }

    @Bean
    public FirebaseDatabase firebaseDatabase(FirebaseApp firebaseApp) {
        return FirebaseDatabase.getInstance(firebaseApp);
    }
}
