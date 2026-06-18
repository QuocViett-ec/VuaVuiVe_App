package vn.vuavuive.shared.data.dto;

public class CategoryResponse {
    private String id;
    private String name;
    private String slug;
    private String imageUrl;
    private String parentId;
    private String parentName;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getParentId() {
        return parentId;
    }

    public String getParentName() {
        return parentName;
    }
}
