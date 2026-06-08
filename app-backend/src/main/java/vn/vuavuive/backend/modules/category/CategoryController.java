package vn.vuavuive.backend.modules.category;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.vuavuive.backend.modules.category.dto.CategoryRequest;
import vn.vuavuive.backend.modules.category.dto.CategoryResponse;

import java.util.List;
import java.util.UUID;

/**
 * CategoryController — API quản lý danh mục sản phẩm.
 *
 * PUBLIC (Không cần token):
 *   GET /api/categories           — Lấy tất cả danh mục gốc
 *   GET /api/categories/{id}/children — Lấy danh mục con
 *
 * ADMIN/STAFF (Cần token + role):
 *   POST   /api/categories        — Tạo danh mục mới
 *   PUT    /api/categories/{id}   — Cập nhật danh mục
 *   DELETE /api/categories/{id}   — Xóa mềm danh mục
 */
@Tag(name = "Categories", description = "API quản lý danh mục sản phẩm")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Lấy tất cả danh mục gốc (Có Cache Redis)")
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllRootCategories() {
        return ResponseEntity.ok(categoryService.getAllRootCategories());
    }

    @Operation(summary = "Lấy danh mục con theo ID danh mục cha")
    @GetMapping("/{id}/children")
    public ResponseEntity<List<CategoryResponse>> getChildren(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getChildren(id));
    }

    @Operation(summary = "[ADMIN] Tạo danh mục mới")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCategory(request));
    }

    @Operation(summary = "[ADMIN] Cập nhật danh mục")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @Operation(summary = "[ADMIN] Xóa mềm danh mục")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
