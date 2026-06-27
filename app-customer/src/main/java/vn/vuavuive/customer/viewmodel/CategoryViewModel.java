package vn.vuavuive.customer.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.data.repository.CategoryRepositoryFirebase;
import vn.vuavuive.shared.data.dto.CategoryResponse;
import java.util.List;
import javax.inject.Inject;

/**
 * CategoryViewModel — provides live category data from Firebase Realtime Database.
 * Used in HomeFragment and ProductListFragment to display real categories.
 */
@HiltViewModel
public class CategoryViewModel extends ViewModel {

    private final CategoryRepositoryFirebase categoryRepository;

    @Inject
    public CategoryViewModel(CategoryRepositoryFirebase categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Fetches all root-level categories from Firebase.
     * Returns LiveData wrapping a Result with the list of categories.
     */
    public LiveData<AuthRepository.Result<List<CategoryResponse>>> getCategories() {
        return categoryRepository.getCategories();
    }
}
