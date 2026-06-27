package vn.vuavuive.backend.modules.product;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import vn.vuavuive.backend.core.FirebaseRepositoryHelper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProductRepository {

    private final FirebaseRepositoryHelper firebase;

    public Optional<Product> findById(String id) {
        return Optional.ofNullable(firebase.get("products/" + id, Product.class));
    }

    public Optional<Product> findById(UUID id) {
        return findById(id.toString());
    }

    public List<Product> findAll() {
        return firebase.getList("products", Product.class);
    }

    public Product save(Product product) {
        if (product.getId() == null) {
            product.setId(UUID.randomUUID().toString());
        }
        firebase.save("products/" + product.getId(), product);
        return product;
    }

    public void deleteById(String id) {
        firebase.delete("products/" + id);
    }

    private <T> Page<T> paginate(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        if (start > list.size()) {
            return new PageImpl<>(List.of(), pageable, list.size());
        }
        return new PageImpl<>(list.subList(start, end), pageable, list.size());
    }

    public Page<Product> findByNameContainingIgnoreCaseAndIsActiveTrue(String name, Pageable pageable) {
        String lowerName = name == null ? "" : name.toLowerCase();
        List<Product> filtered = findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()) && p.getName() != null && p.getName().toLowerCase().contains(lowerName))
                .collect(Collectors.toList());
        return paginate(filtered, pageable);
    }

    public Page<Product> findByCategoryIdAndIsActiveTrue(UUID categoryId, Pageable pageable) {
        if (categoryId == null) return new PageImpl<>(List.of(), pageable, 0);
        String catIdStr = categoryId.toString();
        List<Product> filtered = findAll().stream()
                .filter(p -> catIdStr.equals(p.getCategoryId()) && Boolean.TRUE.equals(p.getIsActive()))
                .collect(Collectors.toList());
        return paginate(filtered, pageable);
    }

    public Page<Product> findAvailableProducts(Pageable pageable) {
        List<Product> filtered = findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()) && p.getStockQuantity() != null && p.getStockQuantity() > 0)
                .collect(Collectors.toList());
        return paginate(filtered, pageable);
    }

    public Page<Product> searchCatalogForApp(String category, String search, Pageable pageable) {
        String searchLower = search == null ? "" : search.toLowerCase();
        List<Product> filtered = findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()) && p.getStockQuantity() != null && p.getStockQuantity() > 0)
                .filter(p -> {
                    if (category == null || category.isEmpty() || "all".equalsIgnoreCase(category)) {
                        return true;
                    }
                    // Ở Firebase ta so khớp categoryId hoặc slug
                    return category.equals(p.getCategoryId()) || category.equals(p.getSlug());
                })
                .filter(p -> {
                    if (searchLower.isEmpty()) return true;
                    boolean matchName = p.getName() != null && p.getName().toLowerCase().contains(searchLower);
                    boolean matchTags = p.getTags() != null && p.getTags().toString().toLowerCase().contains(searchLower);
                    boolean matchSubCat = p.getSubCategory() != null && p.getSubCategory().toLowerCase().contains(searchLower);
                    return matchName || matchTags || matchSubCat;
                })
                .collect(Collectors.toList());
        return paginate(filtered, pageable);
    }

    public Optional<Product> findByExternalIdAndIsActiveTrue(String externalId) {
        if (externalId == null) return Optional.empty();
        return findAll().stream()
                .filter(p -> externalId.equals(p.getExternalId()) && Boolean.TRUE.equals(p.getIsActive()))
                .findFirst();
    }

    public Optional<Product> findBySlugAndIsActiveTrue(String slug) {
        if (slug == null) return Optional.empty();
        return findAll().stream()
                .filter(p -> slug.equals(p.getSlug()) && Boolean.TRUE.equals(p.getIsActive()))
                .findFirst();
    }
}
