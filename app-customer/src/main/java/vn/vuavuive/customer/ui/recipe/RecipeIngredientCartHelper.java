package vn.vuavuive.customer.ui.recipe;

import android.content.Context;
import android.widget.Toast;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.viewmodel.CartViewModel;
import vn.vuavuive.customer.viewmodel.ProductViewModel;
import vn.vuavuive.shared.data.dto.Product;
import vn.vuavuive.shared.data.local.CartItemEntity;

public class RecipeIngredientCartHelper {

    private final Context context;
    private final LifecycleOwner owner;
    private final ProductViewModel productViewModel;
    private final CartViewModel cartViewModel;
    private final List<Runnable> pendingLookups = new ArrayList<>();
    private List<Product> cachedProducts;
    private boolean loadingProducts = false;

    public RecipeIngredientCartHelper(
            Context context,
            LifecycleOwner owner,
            ProductViewModel productViewModel,
            CartViewModel cartViewModel
    ) {
        this.context = context;
        this.owner = owner;
        this.productViewModel = productViewModel;
        this.cartViewModel = cartViewModel;
    }

    public void addIngredient(Map<String, Object> ingredient) {
        addIngredient(ingredient, true);
    }

    public void addIngredient(Map<String, Object> ingredient, boolean showMissingToast) {
        if (ingredient == null) return;
        String ingredientName = value(ingredient, "name", "productName", "title");
        if (ingredientName.isEmpty()) return;

        withProducts(() -> {
            Product product = findProduct(ingredient);
            if (product == null) {
                if (showMissingToast) {
                    Toast.makeText(context, "Không tìm thấy sản phẩm: " + ingredientName, Toast.LENGTH_SHORT).show();
                }
                return;
            }
            addProductToCart(product);
            Toast.makeText(context, "Đã thêm " + product.getName() + " vào giỏ", Toast.LENGTH_SHORT).show();
        });
    }

    private void withProducts(Runnable action) {
        if (cachedProducts != null) {
            action.run();
            return;
        }
        pendingLookups.add(action);
        if (loadingProducts) return;

        loadingProducts = true;
        LiveData<AuthRepository.Result<List<Product>>> liveData =
                productViewModel.getAllActiveProductsOnce(500);
        Observer<AuthRepository.Result<List<Product>>> observer =
                new Observer<AuthRepository.Result<List<Product>>>() {
                    @Override
                    public void onChanged(AuthRepository.Result<List<Product>> result) {
                        if (result == null || result.status == AuthRepository.Result.Status.LOADING) return;
                        liveData.removeObserver(this);
                        loadingProducts = false;

                        cachedProducts = result.status == AuthRepository.Result.Status.SUCCESS && result.data != null
                                ? result.data
                                : new ArrayList<>();

                        List<Runnable> tasks = new ArrayList<>(pendingLookups);
                        pendingLookups.clear();
                        for (Runnable task : tasks) task.run();
                    }
                };
        liveData.observe(owner, observer);
    }

    private Product findProduct(Map<String, Object> ingredient) {
        if (cachedProducts == null || cachedProducts.isEmpty()) return null;

        String productId = value(ingredient, "productId", "product_id", "id");
        if (!productId.isEmpty()) {
            for (Product p : cachedProducts) {
                if (productId.equals(p.getId())) return p;
            }
        }

        String ingredientName = value(ingredient, "name", "productName", "title");
        String query = normalize(ingredientName);
        if (query.isEmpty()) return null;

        Product best = null;
        int bestScore = 0;
        for (Product p : cachedProducts) {
            int score = scoreProduct(p, query);
            if (score > bestScore) {
                best = p;
                bestScore = score;
            }
        }
        return bestScore >= 45 ? best : null;
    }

    private int scoreProduct(Product p, String query) {
        String name = normalize(p.getName());
        if (name.isEmpty()) return 0;

        if (name.equals(query)) return 120;
        if (name.contains(query)) return 105;

        String queryWithoutUnits = removeUnits(query);
        String nameWithoutUnits = removeUnits(name);
        if (nameWithoutUnits.equals(queryWithoutUnits)) return 110;
        if (nameWithoutUnits.contains(queryWithoutUnits)) return 100;

        int aliasScore = scoreAliases(nameWithoutUnits, queryWithoutUnits);
        if (aliasScore > 0) return aliasScore;

        if (allTokensMatch(nameWithoutUnits, queryWithoutUnits)) return 80;

        String haystack = nameWithoutUnits + " "
                + normalize(p.getDescription()) + " "
                + normalize(p.getSubCategory()) + " "
                + normalizeTags(p);
        if (haystack.contains(queryWithoutUnits)) return 65;
        if (allTokensMatch(haystack, queryWithoutUnits)) return 55;
        return 0;
    }

    private int scoreAliases(String productName, String query) {
        Map<String, String[]> aliases = new HashMap<>();
        aliases.put("thit heo ba roi", new String[]{"thit ba roi", "thit ba chi", "thit heo hai lat", "ba roi"});
        aliases.put("hanh la", new String[]{"he la", "hanh", "he"});
        aliases.put("nuoc mam", new String[]{"nuoc tuong", "maggi", "gia vi"});
        aliases.put("bi do", new String[]{"bi xanh", "bau sao", "bi"});
        aliases.put("muoi", new String[]{"gia vi", "bo hu gia vi"});
        aliases.put("bot ngot", new String[]{"gia vi", "bo hu gia vi"});

        for (Map.Entry<String, String[]> entry : aliases.entrySet()) {
            String key = entry.getKey();
            if (!query.contains(key) && !key.contains(query)) continue;
            for (String alias : entry.getValue()) {
                if (productName.contains(alias)) return 75;
            }
        }
        return 0;
    }

    private boolean allTokensMatch(String value, String query) {
        Set<String> stopWords = Set.of("tuoi", "song", "da", "lat", "dac", "san", "ngon");
        for (String token : query.split("\\s+")) {
            if (token.length() < 2 || stopWords.contains(token)) continue;
            if (!value.contains(token)) return false;
        }
        return true;
    }

    private String normalizeTags(Product p) {
        if (p.getTags() == null) return "";
        StringBuilder sb = new StringBuilder();
        for (String tag : p.getTags()) {
            sb.append(' ').append(normalize(tag));
        }
        return sb.toString();
    }

    private String removeUnits(String value) {
        return value
                .replaceAll("\\([^)]*\\)", " ")
                .replaceAll("\\b\\d+[\\d.,]*\\s*(kg|g|gram|ml|l|lit|hop|goi|chai|lon|bo|bich|chiec)\\b", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String normalize(String value) {
        if (value == null) return "";
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s()]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return normalized;
    }

    private String value(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object val = map.get(key);
            if (val != null) {
                String text = val.toString().trim();
                if (!text.isEmpty()) return text;
            }
        }
        return "";
    }

    private void addProductToCart(Product p) {
        CartItemEntity item = new CartItemEntity();
        item.setProductId(p.getId());
        item.setProductName(p.getName());
        item.setProductPrice(p.getPrice());
        item.setProductImageUrl(p.getImageUrl());
        item.setProductUnit(p.getUnit());
        item.setProductStock(p.getStock());
        item.setQuantity(1);
        item.setAddedAt(System.currentTimeMillis());
        item.setSavedForLater(false);
        cartViewModel.addItem(item);
    }
}
