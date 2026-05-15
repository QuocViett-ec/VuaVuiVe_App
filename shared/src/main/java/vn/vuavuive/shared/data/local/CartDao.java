package vn.vuavuive.shared.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface CartDao {

    @Query("SELECT * FROM cart_items WHERE savedForLater = 0 ORDER BY addedAt ASC")
    LiveData<List<CartItemEntity>> getCartItems();

    /** Alias: savedForLater items — used by CartFragment */
    @Query("SELECT * FROM cart_items WHERE savedForLater = 1 ORDER BY addedAt ASC")
    LiveData<List<CartItemEntity>> getSavedItems();

    @Query("SELECT * FROM cart_items WHERE savedForLater = 1 ORDER BY addedAt ASC")
    LiveData<List<CartItemEntity>> getSavedForLaterItems();

    @Query("SELECT * FROM cart_items ORDER BY addedAt ASC")
    List<CartItemEntity> getAllCartItemsSync();

    /** Alias used by CartRepository */
    @Query("SELECT * FROM cart_items ORDER BY addedAt ASC")
    List<CartItemEntity> getAllSync();

    @Query("SELECT * FROM cart_items WHERE productId = :productId LIMIT 1")
    CartItemEntity getCartItem(String productId);

    /** Alias: insert or update — used by CartRepository */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(CartItemEntity item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(CartItemEntity item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CartItemEntity> items);

    @Delete
    void delete(CartItemEntity item);

    /** Delete by productId string — used by CartRepository */
    @Query("DELETE FROM cart_items WHERE productId = :productId")
    void delete(String productId);

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    void deleteById(String productId);

    @Query("DELETE FROM cart_items WHERE savedForLater = 0")
    void clearCart();

    /** Alias: clear all — used by CartRepository */
    @Query("DELETE FROM cart_items")
    void deleteAll();

    @Query("DELETE FROM cart_items")
    void clearAll();

    @Query("SELECT COUNT(*) FROM cart_items WHERE savedForLater = 0")
    LiveData<Integer> getCartCount();

    @Query("SELECT SUM(productPrice * quantity) FROM cart_items WHERE savedForLater = 0")
    LiveData<Double> getCartTotal();

    /** Update quantity for a specific product */
    @Query("UPDATE cart_items SET quantity = :quantity WHERE productId = :productId")
    void updateQuantity(String productId, int quantity);

    /** Move item between cart and saved-for-later */
    @Query("UPDATE cart_items SET savedForLater = :savedForLater WHERE productId = :productId")
    void setSavedForLater(String productId, boolean savedForLater);

    @Update
    void update(CartItemEntity item);
}

