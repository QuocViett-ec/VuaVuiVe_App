package vn.vuavuive.backend.modules.product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.vuavuive.backend.modules.product.dto.PagedResponse;
import vn.vuavuive.backend.modules.product.dto.ProductRequest;
import vn.vuavuive.backend.modules.product.dto.ProductResponse;

import java.util.UUID;

/**
 * ProductController — API quản lý sản phẩm thực phẩm tươi sống.
 *
 * PUBLIC (Không cần token):
 *   GET /api/products                         — Danh sách (có phân trang, có Cache Redis)
 *   GET /api/products/{id}                    — Chi tiết sản phẩm
 *   GET /api/products/search?keyword=...      — Tìm kiếm
 *   GET /api/products/by-category/{catId}     — Lọc theo danh mục
 *
 * ADMIN/STAFF (Cần token):
 *   POST   /api/products                      — Tạo sản phẩm mới
 *   PUT    /api/products/{id}                 — Cập nhật sản phẩm
 *   PATCH  /api/products/{id}/stock           — Cập nhật tồn kho nhanh
 *   DELETE /api/products/{id}                 — Xóa mềm
 */
@Tag(name = "Products", description = "API quản lý sản phẩm thực phẩm tươi sống")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ===== PUBLIC ENDPOINTS =====

    @Operation(summary = "Lấy danh sách sản phẩm còn hàng (Có Cache, Phân trang)")
    @GetMapping
    public ResponseEntity<PagedResponse<ProductResponse>> getProducts(
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số SP mỗi trang") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.getAvailableProducts(page, size));
    }

    @Operation(summary = "Lấy chi tiết sản phẩm theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @Operation(summary = "Tìm kiếm sản phẩm theo tên")
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<ProductResponse>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.searchProducts(keyword, page, size));
    }

    @Operation(summary = "Lấy sản phẩm theo danh mục (Có Cache)")
    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<PagedResponse<ProductResponse>> getByCategory(
            @PathVariable UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId, page, size));
    }

    // ===== ADMIN / STAFF ENDPOINTS =====

    @Operation(summary = "[ADMIN] Tạo sản phẩm mới")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(request));
    }

    @Operation(summary = "[ADMIN] Cập nhật toàn bộ thông tin sản phẩm")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @Operation(summary = "[STAFF] Cập nhật nhanh số lượng tồn kho")
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ProductResponse> updateStock(
            @PathVariable UUID id,
            @RequestParam int quantity) {
        return ResponseEntity.ok(productService.updateStock(id, quantity));
    }

    @Operation(summary = "[ADMIN] Xóa mềm sản phẩm (Ẩn khỏi danh sách)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
