package vn.vuavuive.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadRoot = Paths.get("uploads").toAbsolutePath().normalize().toUri().toString();
        if (!uploadRoot.endsWith("/")) uploadRoot += "/";
        registry.addResourceHandler("/uploads/**").addResourceLocations(uploadRoot);
    }
}
