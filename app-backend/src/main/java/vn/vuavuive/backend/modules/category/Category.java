package vn.vuavuive.backend.modules.category;

import lombok.*;
import vn.vuavuive.backend.core.BaseEntity;

/**
 * Lớp Category — Danh mục sản phẩm, loại bỏ JPA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    private String name;
    private String slug;
    private String imageUrl;

    @Builder.Default
    private Boolean isActive = true;

    /** ID của danh mục cha (null nếu là danh mục gốc) */
    private String parentId;
}
