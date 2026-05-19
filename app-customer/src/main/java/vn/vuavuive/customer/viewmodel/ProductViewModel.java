package vn.vuavuive.customer.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import vn.vuavuive.customer.data.repository.ProductRepository;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.shared.data.dto.Product;
import vn.vuavuive.shared.data.dto.Review;
import java.util.List;
import javax.inject.Inject;

@HiltViewModel
public class ProductViewModel extends ViewModel {

    private final ProductRepository productRepository;

    // State
    private final MutableLiveData<String> selectedCategory = new MutableLiveData<>("all");
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> sortOrder = new MutableLiveData<>("newest");
    private int currentPage = 1;
    private boolean isLastPage = false;

    @Inject
    public ProductViewModel(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ── Product List ───────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<List<Product>>> loadProducts(int page) {
        currentPage = page;
        String category = selectedCategory.getValue();
        String search = searchQuery.getValue();
        String sort = sortOrder.getValue();
        String cat = "all".equals(category) ? null : category;
        String q = (search == null || search.isEmpty()) ? null : search;

        return productRepository.getProducts(cat, q, page, 20, sort);
    }

    public LiveData<AuthRepository.Result<List<Product>>> refreshProducts() {
        return loadProducts(1);
    }

    public LiveData<AuthRepository.Result<List<Product>>> loadNextPage() {
        if (!isLastPage) {
            return loadProducts(currentPage + 1);
        }
        return new MutableLiveData<>(AuthRepository.Result.success(null));
    }

    // ── Product Detail ─────────────────────────────────────────────────────
    public LiveData<AuthRepository.Result<Product>> getProductDetail(String productId) {
        return productRepository.getProductDetail(productId);
    }

    public LiveData<AuthRepository.Result<List<Review>>> getProductReviews(String productId) {
        return productRepository.getProductReviews(productId);
    }

    public LiveData<AuthRepository.Result<List<Product>>> getSimilarProducts(String productId) {
        return productRepository.getSimilarProducts(productId);
    }

    public LiveData<AuthRepository.Result<List<Product>>> getRecommendations(String userId, int n) {
        return productRepository.getRecommendations(userId, n);
    }

    public LiveData<AuthRepository.Result<Void>> sendRecommendEvent(
            String eventType, String productId, java.util.Map<String, Object> metadata) {
        return productRepository.sendRecommendEvent(eventType, productId, metadata);
    }

    /** Direct repository call — for one-off searches (e.g., Recipe ingredient lookup) */
    public LiveData<AuthRepository.Result<List<Product>>> getProducts(
            String category, String search, int page, int limit, String sort) {
        return productRepository.getProducts(category, search, page, limit, sort);
    }

    // ── Filters ────────────────────────────────────────────────────────────
    public void setCategory(String category) {
        selectedCategory.setValue(category);
    }

    public void setSearch(String query) {
        searchQuery.setValue(query);
    }

    public void setSort(String sort) {
        sortOrder.setValue(sort);
    }

    public MutableLiveData<String> getSelectedCategory() { return selectedCategory; }
    public MutableLiveData<String> getSearchQuery() { return searchQuery; }

    public void setLastPage(boolean lastPage) { this.isLastPage = lastPage; }
    public int getCurrentPage() { return currentPage; }
}
