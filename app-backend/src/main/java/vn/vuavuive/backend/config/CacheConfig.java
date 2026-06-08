package vn.vuavuive.backend.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Cache đơn giản dùng bộ nhớ trong (In-Memory Simple Cache).
 *
 * Chức năng @Cacheable, @CacheEvict trong ProductService và CategoryService
 * vẫn hoạt động bình thường — cache sẽ được lưu vào RAM của ứng dụng
 * thay vì Redis. Phù hợp cho môi trường phát triển và demo đồ án.
 *
 * Lưu ý: Cache sẽ bị mất khi restart ứng dụng (đây là hành vi mong muốn
 * trong môi trường dev vì không cần lo về data stale).
 */
@Configuration
@EnableCaching
public class CacheConfig {
    // Spring Boot tự động cấu hình ConcurrentMapCacheManager
    // khi spring.cache.type=simple trong application-dev.yml
    // Không cần khai báo Bean nào thêm.
}
