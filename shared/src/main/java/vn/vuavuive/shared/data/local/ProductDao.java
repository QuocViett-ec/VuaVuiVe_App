package vn.vuavuive.shared.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ProductDao {

    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY soldCount DESC")
    LiveData<List<ProductEntity>> getAllProducts();

    @Query("SELECT * FROM products WHERE category = :category AND isActive = 1 ORDER BY soldCount DESC")
    LiveData<List<ProductEntity>> getProductsByCategory(String category);

    @Query("SELECT * FROM products WHERE (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') AND isActive = 1")
    LiveData<List<ProductEntity>> searchProducts(String query);

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    ProductEntity getProductSync(String id);

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    LiveData<ProductEntity> getProduct(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ProductEntity> products);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ProductEntity product);

    @Query("DELETE FROM products")
    void clearAll();

    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY soldCount DESC")
    List<ProductEntity> getProductsSync();

    @Query("DELETE FROM products WHERE cachedAt < :expiredBefore")
    void deleteExpiredCache(long expiredBefore);

    @Query("SELECT COUNT(*) FROM products WHERE stock <= 5 AND isActive = 1")
    int getLowStockCount();
}
