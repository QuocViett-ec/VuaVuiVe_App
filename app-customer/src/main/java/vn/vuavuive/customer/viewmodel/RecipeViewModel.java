package vn.vuavuive.customer.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.vuavuive.shared.data.api.RecipeApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;

@HiltViewModel
public class RecipeViewModel extends ViewModel {

    private final RecipeApi recipeApi;
    private final MutableLiveData<List<Map<String, Object>>> recipes = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Object>> currentRecipe = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    @Inject
    public RecipeViewModel(RecipeApi recipeApi) {
        this.recipeApi = recipeApi;
    }

    public LiveData<List<Map<String, Object>>> getRecipes() { return recipes; }
    public LiveData<Map<String, Object>> getCurrentRecipe() { return currentRecipe; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadRecipes() {
        recipeApi.getRecipes().enqueue(new Callback<ApiResponse<List<Map<String, Object>>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Map<String, Object>>>> call,
                                   Response<ApiResponse<List<Map<String, Object>>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    recipes.postValue(response.body().getData());
                } else {
                    errorMessage.postValue("Lỗi lấy danh sách công thức: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<Map<String, Object>>>> call, Throwable t) {
                errorMessage.postValue("Lỗi kết nối mạng: " + t.getMessage());
            }
        });
    }

    public void loadRecipeDetail(String recipeId) {
        recipeApi.getRecipe(recipeId).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call,
                                   Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentRecipe.postValue(response.body().getData());
                } else {
                    errorMessage.postValue("Lỗi tải chi tiết: Không tìm thấy món ăn này (Mã lỗi " + response.code() + ")");
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                errorMessage.postValue("Không thể kết nối Backend. Vui lòng bật Server! Lỗi: " + t.getMessage());
            }
        });
    }
}
