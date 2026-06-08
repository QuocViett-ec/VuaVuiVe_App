package vn.vuavuive.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Custom Runtime Exception cho các lỗi nghiệp vụ.
 * Ném exception này từ Service layer để GlobalExceptionHandler bắt và trả về JSON đẹp.
 *
 * Cách dùng:
 *   throw new AppException(HttpStatus.BAD_REQUEST, "Sản phẩm đã hết hàng");
 *   throw new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng");
 *   throw new AppException(HttpStatus.CONFLICT, "Email đã được đăng ký");
 */
@Getter
public class AppException extends RuntimeException {

    private final HttpStatus status;

    public AppException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    // ===== Các factory methods tiện lợi =====

    public static AppException notFound(String entity) {
        return new AppException(HttpStatus.NOT_FOUND, entity + " không tồn tại");
    }

    public static AppException badRequest(String message) {
        return new AppException(HttpStatus.BAD_REQUEST, message);
    }

    public static AppException conflict(String message) {
        return new AppException(HttpStatus.CONFLICT, message);
    }

    public static AppException forbidden(String message) {
        return new AppException(HttpStatus.FORBIDDEN, message);
    }
}
