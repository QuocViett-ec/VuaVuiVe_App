package vn.vuavuive.shared.data.api;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.CategoryResponse;

/**
 * CategoryApi — Public API for fetching product categories.
 * No authentication required.
 */
public interface CategoryApi {

    /**
     * GET /api/categories
     * Returns all root-level categories from the backend.
     */
    @GET("api/categories")
    Call<ApiResponse<List<CategoryResponse>>> getCategories();

    /**
     * GET /api/categories/{id}/children
     * Returns child categories for a given parent category ID.
     */
    @GET("api/categories/{id}/children")
    Call<ApiResponse<List<CategoryResponse>>> getChildren(
            @retrofit2.http.Path("id") String parentId
    );
}
