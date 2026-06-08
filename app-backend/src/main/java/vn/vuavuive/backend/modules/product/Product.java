package vn.vuavuive.backend.modules.product;

import jakarta.persistence.*;
import lombok.*;
import vn.vuavuive.backend.core.BaseEntity;
import vn.vuavuive.backend.modules.category.Category;

import java.math.BigDecimal;

/**
 * Bảng PRODUCTS — Sản phẩm thực phẩm tươi sống.
 * Mỗi sản phẩm thuộc về một danh mục (Category).
 * Ảnh được lưu URL trên Cloudinary, không lưu nhị phân vào DB.
 */
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

    /** Giá gốc (trước khuyến mãi) */
    @Column(name = "original_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal originalPrice;

    /** Giá bán thực tế */
    @Column(name = "selling_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    /** Số lượng tồn kho */
    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    /**
     * Đơn vị tính: KG, BUNCH (bó), BOX (thùng/hộp), PIECE (con/quả)
     * Quan trọng với thực phẩm tươi sống (500g thịt bò, 1 bó rau muống...)
     */
    @Column(name = "unit", nullable = false)
    @Builder.Default
    private String unit = "KG";

    /** URL ảnh trên Cloudinary */
    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
