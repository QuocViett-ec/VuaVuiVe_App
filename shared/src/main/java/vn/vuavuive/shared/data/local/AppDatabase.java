package vn.vuavuive.shared.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import vn.vuavuive.shared.util.Constants;

/**
 * AppDatabase — Room database class.
 * Version: {@link Constants#DATABASE_VERSION}
 *
 * Khi thêm entity mới hoặc thay đổi schema:
 * 1. Tăng DATABASE_VERSION trong Constants.java
 * 2. Thêm Migration object vào DatabaseModule
 */
@Database(
    entities = {
        CartItemEntity.class,
        ProductEntity.class
    },
    version = Constants.DATABASE_VERSION,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract CartDao cartDao();
    public abstract ProductDao productDao();
}
