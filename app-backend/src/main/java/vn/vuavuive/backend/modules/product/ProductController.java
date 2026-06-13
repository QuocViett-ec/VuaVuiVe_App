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
import vn.vuavuive.backend.core.ApiResponse;
import vn.vuavuive.backend.modules.product.dto.PagedResponse;
import vn.vuavuive.backend.modules.product.dto.ProductRequest;
import vn.vuavuive.backend.modules.product.dto.ProductResponse;

import java.util.List;
import java.util.UUID;

@Tag(name = "Products", description = "Product catalog APIs")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Android-compatible product list")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @Parameter(description = "1-based page for Android compatibility") @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort
    ) {
        return ResponseEntity.ok(productService.getProductsForApp(category, search, page, limit, size, sort));
    }

    @Operation(summary = "Android-compatible category slugs")
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        return ResponseEntity.ok(productService.getCategorySlugsForApp());
    }

    @Operation(summary = "Search products by name")
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<ProductResponse>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.searchProducts(keyword, page, size));
    }

    @Operation(summary = "Get products by category UUID")
    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<PagedResponse<ProductResponse>> getByCategory(
            @PathVariable UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId, page, size));
    }

    @Operation(summary = "Android-compatible product detail by UUID, externalId, or slug")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable String id) {
        return ResponseEntity.ok(productService.getProductForApp(id));
    }

    @Operation(summary = "[ADMIN] Create product")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(request));
    }

    @Operation(summary = "[ADMIN] Update product")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @Operation(summary = "[STAFF] Update product stock")
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ProductResponse> updateStock(
            @PathVariable UUID id,
            @RequestParam int quantity) {
        return ResponseEntity.ok(productService.updateStock(id, quantity));
    }

    @Operation(summary = "[ADMIN] Soft-delete product")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
