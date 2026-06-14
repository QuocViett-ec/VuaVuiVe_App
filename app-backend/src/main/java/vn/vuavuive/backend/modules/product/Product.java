package vn.vuavuive.backend.modules.product;

import jakarta.persistence.*;
import lombok.*;
import vn.vuavuive.backend.core.BaseEntity;
import vn.vuavuive.backend.modules.category.Category;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "original_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "selling_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(name = "unit", nullable = false)
    @Builder.Default
    private String unit = "KG";

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "slug")
    private String slug;

    @Column(name = "sub_category")
    private String subCategory;

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    @Column(name = "external_id")
    private String externalId;
}
