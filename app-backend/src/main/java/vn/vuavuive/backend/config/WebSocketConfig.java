package vn.vuavuive.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocketConfig - Cấu hình Spring WebSockets realtime bằng giao thức STOMP.
 * Phục vụ việc cập nhật trạng thái đơn hàng và vị trí shipper realtime lên Admin Dashboard.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Kích hoạt simple message broker trên các prefix /topic (broadcast) và /queue (1-1)
        config.enableSimpleBroker("/topic", "/queue");
        // Prefix cho các request gửi từ client đến backend (@MessageMapping)
        config.setApplicationDestinationPrefixes("/app");
        // Prefix dành cho tin nhắn cá nhân (User Destination)
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint kết nối WebSocket: ws://localhost:8080/ws
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*"); // Hỗ trợ kết nối chéo domain (CORS)

        // Hỗ trợ SockJS làm fallback nếu trình duyệt/thiết bị không hỗ trợ WebSocket thuần
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
