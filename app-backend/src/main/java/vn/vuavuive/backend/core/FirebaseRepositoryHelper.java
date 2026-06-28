package vn.vuavuive.backend.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Slf4j
@Component
public class FirebaseRepositoryHelper {

    private final String databaseUrl;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public FirebaseRepositoryHelper(
            @Value("${app.firebase.database-url}") String databaseUrl) {
        this.databaseUrl = databaseUrl.endsWith("/") ? databaseUrl.substring(0, databaseUrl.length() - 1) : databaseUrl;
        this.objectMapper = new ObjectMapper()
                .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setPropertyNamingStrategy(com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        log.info("Initialized REST-based FirebaseRepositoryHelper with URL: {}", this.databaseUrl);
    }

    private String buildUrl(String path) {
        String cleanPath = path.startsWith("/") ? path.substring(1) : path;
        return databaseUrl + "/" + cleanPath + ".json";
    }

    /**
     * Lấy giá trị đơn lẻ từ một đường dẫn Firebase
     */
    public <T> T get(String path, Class<T> clazz) {
        String url = buildUrl(path);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String body = response.body();
                if (body == null || body.trim().equals("null") || body.trim().isEmpty()) {
                    return null;
                }
                return objectMapper.readValue(body, clazz);
            } else {
                log.error("Firebase REST GET failed at path: {}, status: {}, body: {}", path, response.statusCode(), response.body());
                return null;
            }
        } catch (Exception e) {
            log.error("Error executing Firebase REST GET at path: {}", path, e);
            return null;
        }
    }

    /**
     * Lấy danh sách các đối tượng từ một node Firebase (dạng Map key-value thành List)
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String path, Class<T> clazz) {
        String url = buildUrl(path);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            List<T> list = new ArrayList<>();
            if (response.statusCode() == 200) {
                String body = response.body();
                if (body == null || body.trim().equals("null") || body.trim().isEmpty()) {
                    return list;
                }
                // Firebase có thể trả về JSON Array nếu keys là số liên tiếp (0, 1, 2...) hoặc JSON Object
                if (body.trim().startsWith("[")) {
                    List<?> rawList = objectMapper.readValue(body, List.class);
                    for (Object rawItem : rawList) {
                        if (rawItem != null) {
                            T item = objectMapper.convertValue(rawItem, clazz);
                            list.add(item);
                        }
                    }
                } else {
                    Map<?, ?> map = objectMapper.readValue(body, Map.class);
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        if (entry.getValue() != null) {
                            T item = objectMapper.convertValue(entry.getValue(), clazz);
                            list.add(item);
                        }
                    }
                }
            } else {
                log.error("Firebase REST GET list failed at path: {}, status: {}, body: {}", path, response.statusCode(), response.body());
            }
            return list;
        } catch (Exception e) {
            log.error("Error executing Firebase REST GET list at path: {}", path, e);
            return Collections.emptyList();
        }
    }

    /**
     * Ghi đè hoặc thêm mới một đối tượng tại đường dẫn cụ thể
     */
    public <T> void save(String path, T value) {
        String url = buildUrl(path);
        try {
            String json = objectMapper.writeValueAsString(value);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error("Firebase REST PUT failed at path: {}, status: {}, body: {}", path, response.statusCode(), response.body());
                throw new RuntimeException("Firebase REST PUT failed");
            }
        } catch (Exception e) {
            log.error("Error executing Firebase REST PUT at path: {}", path, e);
            throw new RuntimeException("Firebase write failed", e);
        }
    }

    /**
     * Cập nhật các trường cụ thể của một node
     */
    public void update(String path, Map<String, Object> updates) {
        String url = buildUrl(path);
        try {
            String json = objectMapper.writeValueAsString(updates);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error("Firebase REST PATCH failed at path: {}, status: {}, body: {}", path, response.statusCode(), response.body());
                throw new RuntimeException("Firebase REST PATCH failed");
            }
        } catch (Exception e) {
            log.error("Error executing Firebase REST PATCH at path: {}", path, e);
            throw new RuntimeException("Firebase update failed", e);
        }
    }

    /**
     * Xóa dữ liệu tại đường dẫn cụ thể
     */
    public void delete(String path) {
        String url = buildUrl(path);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .DELETE()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error("Firebase REST DELETE failed at path: {}, status: {}, body: {}", path, response.statusCode(), response.body());
                throw new RuntimeException("Firebase REST DELETE failed");
            }
        } catch (Exception e) {
            log.error("Error executing Firebase REST DELETE at path: {}", path, e);
            throw new RuntimeException("Firebase delete failed", e);
        }
    }

    /**
     * Tạo một ID ngẫu nhiên duy nhất tại đường dẫn cụ thể
     */
    public String generateId(String path) {
        return UUID.randomUUID().toString();
    }
}
