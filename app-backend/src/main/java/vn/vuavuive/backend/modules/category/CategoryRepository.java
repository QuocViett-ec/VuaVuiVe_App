package vn.vuavuive.backend.modules.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.vuavuive.backend.core.FirebaseRepositoryHelper;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CategoryRepository {

    private final FirebaseRepositoryHelper firebase;

    public Optional<Category> findById(String id) {
        return Optional.ofNullable(firebase.get("categories/" + id, Category.class));
    }

    public Optional<Category> findById(UUID id) {
        return findById(id.toString());
    }

    public List<Category> findAll() {
        return firebase.getList("categories", Category.class);
    }

    public Category save(Category category) {
        if (category.getId() == null) {
            category.setId(UUID.randomUUID().toString());
        }
        firebase.save("categories/" + category.getId(), category);
        return category;
    }

    public void deleteById(String id) {
        firebase.delete("categories/" + id);
    }

    public List<Category> findAllRootCategories() {
        return findAll().stream()
                .filter(c -> c.getParentId() == null && Boolean.TRUE.equals(c.getIsActive()))
                .sorted(Comparator.comparing(Category::getName, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
    }

    public List<Category> findByParentIdAndIsActiveTrueOrderByName(UUID parentId) {
        if (parentId == null) return List.of();
        String parentIdStr = parentId.toString();
        return findAll().stream()
                .filter(c -> parentIdStr.equals(c.getParentId()) && Boolean.TRUE.equals(c.getIsActive()))
                .sorted(Comparator.comparing(Category::getName, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
    }

    public Optional<Category> findBySlugAndIsActiveTrue(String slug) {
        if (slug == null) return Optional.empty();
        return findAll().stream()
                .filter(c -> slug.equals(c.getSlug()) && Boolean.TRUE.equals(c.getIsActive()))
                .findFirst();
    }

    public boolean existsBySlug(String slug) {
        if (slug == null) return false;
        return findAll().stream()
                .anyMatch(c -> slug.equals(c.getSlug()));
    }
}
