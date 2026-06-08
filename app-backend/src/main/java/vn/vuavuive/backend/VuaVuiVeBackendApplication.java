package vn.vuavuive.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point chính của ứng dụng Vựa Vui Vẻ Backend.
 *
 * Các annotation được bật:
 * - @EnableCaching    : Kích hoạt cơ chế Cache (Redis) với @Cacheable
 * - @EnableAsync      : Kích hoạt xử lý bất đồng bộ @Async (cho Chatbot Gemini)
 * - @EnableScheduling : Kích hoạt tác vụ nền @Scheduled (cho auto-cancel đơn hàng)
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class VuaVuiVeBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(VuaVuiVeBackendApplication.class, args);
    }
}
