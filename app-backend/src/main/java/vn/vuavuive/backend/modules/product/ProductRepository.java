package vn.vuavuive.backend.modules.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findByNameContainingIgnoreCaseAndIsActiveTrue(String name, Pageable pageable);

    Page<Product> findByCategoryIdAndIsActiveTrue(UUID categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.stockQuantity > 0")
    Page<Product> findAvailableProducts(Pageable pageable);

    @Query("""
            SELECT p FROM Product p JOIN p.category c
            WHERE p.isActive = true
              AND p.stockQuantity > 0
              AND (:category IS NULL OR :category = '' OR :category = 'all' OR c.slug = :category)
              AND (
                    :search IS NULL OR :search = ''
                    OR lower(p.name) LIKE lower(concat('%', :search, '%'))
                    OR lower(coalesce(p.tags, '')) LIKE lower(concat('%', :search, '%'))
                    OR lower(coalesce(p.subCategory, '')) LIKE lower(concat('%', :search, '%'))
                  )
            """)
    Page<Product> searchCatalogForApp(
            @Param("category") String category,
            @Param("search") String search,
            Pageable pageable
    );

    Optional<Product> findByExternalIdAndIsActiveTrue(String externalId);

    Optional<Product> findBySlugAndIsActiveTrue(String slug);
}
