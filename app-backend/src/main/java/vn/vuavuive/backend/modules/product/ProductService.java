package vn.vuavuive.backend.modules.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuavuive.backend.core.ApiResponse;
import vn.vuavuive.backend.core.Pagination;
import vn.vuavuive.backend.exception.AppException;
import vn.vuavuive.backend.modules.category.Category;
import vn.vuavuive.backend.modules.category.CategoryRepository;
import vn.vuavuive.backend.modules.product.dto.PagedResponse;
import vn.vuavuive.backend.modules.product.dto.ProductRequest;
import vn.vuavuive.backend.modules.product.dto.ProductResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ApiResponse<List<ProductResponse>> getProductsForApp(
            String category,
            String search,
            int page,
            Integer limit,
            Integer size,
            String sort
    ) {
        int pageNumber = Math.max(1, page);
        int pageSize = Math.max(1, Math.min(100, limit != null ? limit : (size != null ? size : 20)));
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sortForApp(sort));
        Page<Product> products = productRepository.searchCatalogForApp(normalizeFilter(category), normalizeFilter(search), pageable);

        return ApiResponse.<List<ProductResponse>>builder()
                .success(true)
                .message("Thao tác thành công")
                .data(products.getContent().stream().map(this::toResponse).toList())
                .pagination(new Pagination(
                        Math.toIntExact(products.getTotalElements()),
                        pageNumber,
                        pageSize,
                        products.getTotalPages()
                ))
                .build();
    }

    public ApiResponse<ProductResponse> getProductForApp(String id) {
        Product product = findProductByAnyId(id);
        return ApiResponse.success(toResponse(product));
    }

    public ApiResponse<List<String>> getCategorySlugsForApp() {
        List<String> categories = categoryRepository.findAllRootCategories()
                .stream()
                .map(Category::getSlug)
                .filter(slug -> slug != null && !slug.isBlank())
                .toList();
        return ApiResponse.success(categories);
    }

    @Cacheable(value = "product-page", key = "'page-' + #page + '-size-' + #size")
    public PagedResponse<ProductResponse> getAvailableProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> products = productRepository.findAvailableProducts(pageable);
        return toPagedResponse(products);
    }

    public PagedResponse<ProductResponse> searchProducts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Product> products = productRepository
                .findByNameContainingIgnoreCaseAndIsActiveTrue(keyword, pageable);
        return toPagedResponse(products);
    }

    @Cacheable(value = "product-page", key = "'cat-' + #categoryId + '-page-' + #page")
    public PagedResponse<ProductResponse> getProductsByCategory(UUID categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Product> products = productRepository
                .findByCategoryIdAndIsActiveTrue(categoryId, pageable);
        return toPagedResponse(products);
    }

    private boolean isAdminOrStaff() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ROLE_STAFF") || role.equals("ROLE_AUDIT"));
    }

    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Sản phẩm"));
        if (!isAdminOrStaff() && !Boolean.TRUE.equals(product.getIsActive())) {
            throw AppException.notFound("Sản phẩm");
        }
        return toResponse(product);
    }

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
                .images(request.images())
                .categoryId(category.getId())
                .isActive(request.isActive() != null ? request.isActive() : true)
                .build();

        return toResponse(productRepository.save(product));
    }

    @Transactional
    @CacheEvict(value = {"products", "product-page"}, allEntries = true)
    public ProductResponse updateProduct(UUID id, ProductRequest request) {
        System.out.println("DEBUG UPDATEPRODUCT: id=" + id + ", request=" + request + ", request.isActive()=" + request.isActive());
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
        product.setCategoryId(category.getId());
        if (request.imageUrl() != null) product.setImageUrl(request.imageUrl());
        if (request.images() != null) product.setImages(request.images());
        if (request.isActive() != null) product.setIsActive(request.isActive());

        return toResponse(productRepository.save(product));
    }

    @Transactional
    @CacheEvict(value = {"products", "product-page"}, allEntries = true)
    public ProductResponse updateStock(UUID id, int newQuantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Sản phẩm"));
        product.setStockQuantity(newQuantity);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    @CacheEvict(value = {"products", "product-page"}, allEntries = true)
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Sản phẩm"));
        product.setIsActive(false);
        productRepository.save(product);
    }

    private Product findProductByAnyId(String id) {
        if (id == null || id.isBlank()) {
            throw AppException.notFound("Sản phẩm");
        }
        boolean bypass = isAdminOrStaff();
        try {
            UUID uuid = UUID.fromString(id);
            return productRepository.findById(uuid)
                    .filter(product -> bypass || Boolean.TRUE.equals(product.getIsActive()))
                    .orElseThrow(() -> AppException.notFound("Sản phẩm"));
        } catch (IllegalArgumentException ignored) {
            return productRepository.findByExternalIdAndIsActiveTrue(id)
                    .or(() -> productRepository.findBySlugAndIsActiveTrue(id))
                    .or(() -> productRepository.findById(id))
                    .filter(product -> bypass || Boolean.TRUE.equals(product.getIsActive()))
                    .orElseThrow(() -> AppException.notFound("Sản phẩm"));
        }
    }

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
        if (p.getOriginalPrice() != null && p.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0) {
            discountPercent = p.getOriginalPrice()
                    .subtract(p.getSellingPrice())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(p.getOriginalPrice(), RoundingMode.HALF_UP)
                    .intValue();
        }

        String externalId = p.getExternalId();
        Double rating = fallbackRating(externalId != null ? externalId : String.valueOf(p.getId()));

        Category category = p.getCategoryId() != null
                ? categoryRepository.findById(p.getCategoryId()).orElse(null)
                : null;

        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getSlug(),
                p.getDescription(),
                p.getOriginalPrice(),
                p.getSellingPrice(),
                p.getStockQuantity(),
                p.getUnit(),
                p.getImageUrl(),
                ProductImages.withFallback(p),
                p.getIsActive(),
                category != null ? category.getId() : null,
                category != null ? category.getName() : null,
                category != null ? category.getSlug() : null,
                p.getSubCategory(),
                parseTags(p.getTags()),
                parseExternalId(externalId),
                rating,
                0,
                0,
                Math.max(0, discountPercent)
        );
    }

    private Integer parseExternalId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(raw);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private List<String> parseTags(Object raw) {
        if (raw == null) {
            return Collections.emptyList();
        }
        if (raw instanceof List) {
            List<?> list = (List<?>) raw;
            List<String> strList = new ArrayList<>();
            for (Object item : list) {
                if (item != null) {
                    strList.add(item.toString());
                }
            }
            return strList;
        }
        String str = raw.toString();
        if (str.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(str, new TypeReference<List<String>>() {});
        } catch (Exception ignored) {
            return List.of(str);
        }
    }

    private Sort sortForApp(String sort) {
        if (sort == null || sort.isBlank() || "newest".equalsIgnoreCase(sort)) {
            return Sort.by("createdAt").descending();
        }
        if ("price_asc".equalsIgnoreCase(sort)) {
            return Sort.by("sellingPrice").ascending();
        }
        if ("price_desc".equalsIgnoreCase(sort)) {
            return Sort.by("sellingPrice").descending();
        }
        if ("name".equalsIgnoreCase(sort)) {
            return Sort.by("name").ascending();
        }
        return Sort.by("createdAt").descending();
    }

    private String normalizeFilter(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private Double fallbackRating(String key) {
        if (key == null || key.isBlank()) {
            return 4.5;
        }
        int sum = 0;
        for (int i = 0; i < key.length(); i++) {
            sum += key.charAt(i);
        }
        int score = 42 + (sum % 8);
        return score / 10.0;
    }
}
