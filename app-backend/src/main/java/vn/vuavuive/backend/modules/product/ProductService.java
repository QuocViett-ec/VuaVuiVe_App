package vn.vuavuive.backend.modules.product;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuavuive.backend.exception.AppException;
import vn.vuavuive.backend.modules.category.Category;
import vn.vuavuive.backend.modules.category.CategoryRepository;
import vn.vuavuive.backend.modules.product.dto.PagedResponse;
import vn.vuavuive.backend.modules.product.dto.ProductRequest;
import vn.vuavuive.backend.modules.product.dto.ProductResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // =================== PUBLIC APIs (Có Cache) ===================

    /**
     * Lấy tất cả sản phẩm available, có phân trang.
     * Cache key bao gồm page và size để mỗi trang có cache riêng.
     * App Android gọi API này khi cuộn xuống (Infinite Scroll / Load More).
     */
    @Cacheable(value = "product-page", key = "'page-' + #page + '-size-' + #size")
    public PagedResponse<ProductResponse> getAvailableProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> products = productRepository.findAvailableProducts(pageable);
        return toPagedResponse(products);
    }

    /**
     * Tìm kiếm sản phẩm theo tên (Search bar).
     * Không cache vì keyword rất đa dạng.
     */
    public PagedResponse<ProductResponse> searchProducts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Product> products = productRepository
                .findByNameContainingIgnoreCaseAndIsActiveTrue(keyword, pageable);
        return toPagedResponse(products);
    }

    /**
     * Lấy sản phẩm theo danh mục.
     * Cache theo categoryId để không phải query DB mỗi lần.
     */
    @Cacheable(value = "product-page", key = "'cat-' + #categoryId + '-page-' + #page")
    public PagedResponse<ProductResponse> getProductsByCategory(UUID categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Product> products = productRepository
                .findByCategoryIdAndIsActiveTrue(categoryId, pageable);
        return toPagedResponse(products);
    }

    /**
     * Lấy chi tiết một sản phẩm theo ID.
     * Cache riêng từng sản phẩm theo ID.
     */
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Sản phẩm"));
        if (!product.getIsActive()) {
            throw AppException.notFound("Sản phẩm");
        }
        return toResponse(product);
    }

    // =================== ADMIN APIs (Xóa Cache khi thay đổi) ===================

    /**
     * Tạo sản phẩm mới (Admin).
     * Xóa toàn bộ cache product-page sau khi tạo.
     */
    @Transactional
    @CacheEvict(value = {"products", "product-page"}, allEntries = true)
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> AppException.notFound("Danh mục"));

        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .originalPrice(request.originalPrice())
                .sellingPrice(request.sellingPrice())
                .stockQuantity(request.stockQuantity())
                .unit(request.unit())
                .imageUrl(request.imageUrl())
                .category(category)
                .build();

        return toResponse(productRepository.save(product));
    }

    /**
     * Cập nhật sản phẩm (Admin).
     * Xóa cache của sản phẩm cụ thể đó + tất cả product-page.
     */
    @Transactional
    @CacheEvict(value = {"products", "product-page"}, allEntries = true)
    public ProductResponse updateProduct(UUID id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Sản phẩm"));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> AppException.notFound("Danh mục"));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setOriginalPrice(request.originalPrice());
        product.setSellingPrice(request.sellingPrice());
        product.setStockQuantity(request.stockQuantity());
        product.setUnit(request.unit());
        product.setCategory(category);
        if (request.imageUrl() != null) product.setImageUrl(request.imageUrl());

        return toResponse(productRepository.save(product));
    }

    /**
     * Cập nhật số lượng tồn kho nhanh (Admin/Staff).
     */
    @Transactional
    @CacheEvict(value = {"products", "product-page"}, allEntries = true)
    public ProductResponse updateStock(UUID id, int newQuantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Sản phẩm"));
        product.setStockQuantity(newQuantity);
        return toResponse(productRepository.save(product));
    }

    /**
     * Xóa mềm sản phẩm (Admin).
     */
    @Transactional
    @CacheEvict(value = {"products", "product-page"}, allEntries = true)
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Sản phẩm"));
        product.setIsActive(false);
        productRepository.save(product);
    }

    // =================== Helpers ===================

    private PagedResponse<ProductResponse> toPagedResponse(Page<Product> page) {
        return new PagedResponse<>(
                page.getContent().stream().map(this::toResponse).toList(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.isFirst(),
                page.isLast()
        );
    }

    ProductResponse toResponse(Product p) {
        int discountPercent = 0;
        if (p.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0) {
            discountPercent = p.getOriginalPrice()
                    .subtract(p.getSellingPrice())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(p.getOriginalPrice(), RoundingMode.HALF_UP)
                    .intValue();
        }

        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getOriginalPrice(),
                p.getSellingPrice(),
                p.getStockQuantity(),
                p.getUnit(),
                p.getImageUrl(),
                p.getIsActive(),
                p.getCategory().getId(),
                p.getCategory().getName(),
                Math.max(0, discountPercent)
        );
    }
}
