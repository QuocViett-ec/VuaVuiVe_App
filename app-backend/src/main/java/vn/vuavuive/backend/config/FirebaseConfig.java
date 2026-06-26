package vn.vuavuive.backend.config;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${app.firebase.database-url}")
    private String databaseUrl;

    @Value("${app.firebase.config-path}")
    private String configPath;

    @Bean
    public FirebaseApp firebaseApp() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                return FirebaseApp.getInstance();
            }

            log.info("Starting initialization of Firebase Admin SDK...");
            ClassPathResource resource = new ClassPathResource("serviceAccountKey.json");
            
            if (!resource.exists()) {
                log.error("=========================================================================");
                log.error("CRITICAL ERROR: serviceAccountKey.json NOT FOUND!");
                log.error("Please download serviceAccountKey.json from Firebase Console and place it at:");
                log.error("app-backend/src/main/resources/serviceAccountKey.json");
                log.error("=========================================================================");
                throw new IllegalStateException("Missing serviceAccountKey.json credential file for Firebase Admin SDK.");
            }

            GoogleCredentials credentials;
            try (InputStream serviceAccount = resource.getInputStream()) {
                try {
                    credentials = GoogleCredentials.fromStream(serviceAccount);
                } catch (Exception e) {
                    log.warn("=========================================================================");
                    log.warn("WARNING: Failed to parse serviceAccountKey.json private key!");
                    log.warn("Falling back to anonymous credentials. RTDB must have public read/write rules.");
                    log.warn("=========================================================================");
                    credentials = new GoogleCredentials() {
                        @Override
                        public Map<String, List<String>> getRequestMetadata(URI uri) {
                            return Collections.emptyMap();
                        }
                        @Override
                        public boolean hasRequestMetadata() {
                            return false;
                        }
                        @Override
                        public boolean hasRequestMetadataOnly() {
                            return false;
                        }
                        @Override
                        public void refresh() {}
                    };
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
