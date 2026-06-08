package vn.vuavuive.backend.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Cấu hình các Bean dùng chung toàn ứng dụng.
 */
@Configuration
public class AppConfig {

    /** ModelMapper — Tự động map Entity <-> DTO */
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    /** RestTemplate — Dùng để gọi HTTP API bên ngoài (MoMo, Gemini AI...) */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

