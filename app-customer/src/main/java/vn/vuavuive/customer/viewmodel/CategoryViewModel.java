package vn.vuavuive.customer.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.data.repository.CategoryRepository;
import vn.vuavuive.shared.data.dto.CategoryResponse;
import java.util.List;
import javax.inject.Inject;

/**
 * CategoryViewModel — provides live category data from the backend API.
 * Used in HomeFragment and ProductListFragment to display real categories.
 */
@HiltViewModel
public class CategoryViewModel extends ViewModel {

    private final CategoryRepository categoryRepository;

    @Inject
    public CategoryViewModel(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Fetches all root-level categories from the backend.
     * Returns LiveData wrapping a Result with the list of categories.
     * On network failure, returns an empty list (caller falls back to MockDataProvider).
     */
    public LiveData<AuthRepository.Result<List<CategoryResponse>>> getCategories() {
        return categoryRepository.getCategories();
    }
}
