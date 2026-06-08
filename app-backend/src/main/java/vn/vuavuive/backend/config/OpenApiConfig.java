package vn.vuavuive.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenApiConfig - Cấu hình Swagger UI để kiểm thử và tài liệu hóa API.
 * Hỗ trợ xác thực bằng JWT Bearer Token trực tiếp trên giao diện Swagger.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "Bearer Token";

        return new OpenAPI()
                .info(new Info()
                        .title("Vựa Vui Vẻ Backend API Document")
                        .description("Tài liệu API hệ thống TMĐT thực phẩm tươi sống, nông sản sạch Vựa Vui Vẻ. "
                                + "Hệ thống hỗ trợ Đặt hàng, Thanh toán VNPay/MoMo Webhook, Realtime Shipper Tracking qua WebSockets, và Chatbot Gemini AI.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Vua Vui Ve Team")
                                .email("support@vuavuive.vn")))
                // Cấu hình nút Authorize trên Swagger UI để nhập JWT
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Hãy nhập JWT Access Token vào đây để gọi các API được bảo mật.")));
    }
}
