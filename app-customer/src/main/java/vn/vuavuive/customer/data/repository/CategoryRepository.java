package vn.vuavuive.customer.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.vuavuive.shared.data.api.CategoryApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.CategoryResponse;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * CategoryRepository — fetches product categories from the backend API.
 * Falls back to an empty list (caller handles fallback to MockDataProvider).
 */
@Singleton
public class CategoryRepository {

    private final CategoryApi categoryApi;

    @Inject
    public CategoryRepository(CategoryApi categoryApi) {
        this.categoryApi = categoryApi;
    }

    /**
     * Fetches all root-level categories from the backend.
     * Posts SUCCESS with the list, or SUCCESS with empty list on error (caller handles fallback).
     */
    public LiveData<AuthRepository.Result<List<CategoryResponse>>> getCategories() {
        MutableLiveData<AuthRepository.Result<List<CategoryResponse>>> result = new MutableLiveData<>();
        result.postValue(AuthRepository.Result.loading());

        categoryApi.getCategories().enqueue(new Callback<ApiResponse<List<CategoryResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<CategoryResponse>>> call,
                                   Response<ApiResponse<List<CategoryResponse>>> response) {
                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().isSuccess()
                        && response.body().getData() != null
                        && !response.body().getData().isEmpty()) {
                    result.postValue(AuthRepository.Result.success(response.body().getData()));
                } else {
                    // Return empty list — HomeFragment will fall back to MockDataProvider
                    result.postValue(AuthRepository.Result.success(new ArrayList<>()));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<CategoryResponse>>> call, Throwable t) {
                // Network error — return empty list, caller uses fallback
                result.postValue(AuthRepository.Result.success(new ArrayList<>()));
            }
        });

        return result;
    }
}
