package vn.vuavuive.backend.modules.category;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vuavuive.backend.exception.AppException;
import vn.vuavuive.backend.modules.category.dto.CategoryRequest;
import vn.vuavuive.backend.modules.category.dto.CategoryResponse;

import java.text.Normalizer;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Lấy tất cả danh mục gốc.
     * Kết quả được Cache vào Redis key "categories::all-root" — TTL 30 phút.
     * App Android gọi API này khi hiển thị lưới danh mục trên trang chủ.
     */
    @Cacheable(value = "categories", key = "'all-root'")
    public List<CategoryResponse> getAllRootCategories() {
        return categoryRepository.findAllRootCategories()
                .stream().map(this::toResponse).toList();
    }

    /**
     * Lấy danh mục con của một danh mục cha.
     * Cache theo parentId.
     */
    @Cacheable(value = "categories", key = "'children-' + #parentId")
    public List<CategoryResponse> getChildren(UUID parentId) {
        return categoryRepository.findByParentIdAndIsActiveTrueOrderByName(parentId)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Tạo danh mục mới (Admin).
     * Xóa cache danh mục sau khi tạo để App nhận được data mới nhất.
     */
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse createCategory(CategoryRequest request) {
        String slug = request.slug() != null && !request.slug().isBlank()
                ? request.slug() : generateSlug(request.name());

        if (categoryRepository.existsBySlug(slug)) {
            throw AppException.conflict("Slug '" + slug + "' đã tồn tại");
        }

        String parentId = null;
        if (request.parentId() != null) {
            Category parent = categoryRepository.findById(request.parentId())
                    .orElseThrow(() -> AppException.notFound("Danh mục cha"));
            parentId = parent.getId();
        }

        Category category = Category.builder()
                .name(request.name())
                .slug(slug)
                .imageUrl(request.imageUrl())
                .parentId(parentId)
                .build();

        return toResponse(categoryRepository.save(category));
    }

    /**
     * Cập nhật danh mục (Admin).
     */
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse updateCategory(UUID id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Danh mục"));

        category.setName(request.name());
        if (request.imageUrl() != null) category.setImageUrl(request.imageUrl());

        return toResponse(categoryRepository.save(category));
    }

    /**
     * Xóa mềm danh mục (set isActive = false).
     */
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Danh mục"));
        category.setIsActive(false);
        categoryRepository.save(category);
    }

    // =================== Helpers ===================

    private CategoryResponse toResponse(Category c) {
        Category parent = c.getParentId() != null
                ? categoryRepository.findById(c.getParentId()).orElse(null)
                : null;
        return new CategoryResponse(
                UUID.fromString(c.getId()),
                c.getName(),
                c.getSlug(),
                c.getImageUrl(),
                parent != null ? UUID.fromString(parent.getId()) : null,
                parent != null ? parent.getName() : null
        );
    }

    /** Tạo slug từ tên tiếng Việt: "Rau củ quả" -> "rau-cu-qua" */
    private String generateSlug(String name) {
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized)
                .replaceAll("")
                .toLowerCase()
                .replaceAll("đ", "d")
                .replaceAll("[^a-z0-9\\s]", "")
                .trim()
                .replaceAll("\\s+", "-");
    }
}
