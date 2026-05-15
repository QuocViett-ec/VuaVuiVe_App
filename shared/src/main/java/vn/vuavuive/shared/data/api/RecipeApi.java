package vn.vuavuive.shared.data.api;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.Product;

public interface RecipeApi {

    @GET("api/recipes")
    Call<ApiResponse<List<Map<String, Object>>>> getRecipes();

    @GET("api/recipes/{id}")
    Call<ApiResponse<Map<String, Object>>> getRecipe(@Path("id") String id);
}
