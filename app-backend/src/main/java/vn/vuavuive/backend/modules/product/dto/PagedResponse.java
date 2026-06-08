package vn.vuavuive.backend.modules.product.dto;

import java.util.List;

/**
 * Wrapper cho kết quả phân trang (Pagination) trả về cho App Android.
 * App dùng các trường này để hiển thị nút "Xem thêm" và "Trang X/Y".
 */
public record PagedResponse<T>(
        List<T> content,         // Danh sách item trong trang hiện tại
        int currentPage,         // Trang hiện tại (bắt đầu từ 0)
        int totalPages,          // Tổng số trang
        long totalElements,      // Tổng số item
        boolean isFirst,         // Có phải trang đầu không
        boolean isLast           // Có phải trang cuối không
) {}
