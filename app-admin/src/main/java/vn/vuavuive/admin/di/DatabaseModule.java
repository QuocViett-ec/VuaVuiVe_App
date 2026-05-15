package vn.vuavuive.admin.di;

import android.content.Context;
import androidx.room.Room;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import vn.vuavuive.shared.data.local.AppDatabase;
import vn.vuavuive.shared.data.local.CartDao;
import vn.vuavuive.shared.data.local.ProductDao;
import vn.vuavuive.shared.util.Constants;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public AppDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(
                context,
                AppDatabase.class,
                Constants.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build();
    }

    @Provides @Singleton
    public CartDao provideCartDao(AppDatabase database) { return database.cartDao(); }

    @Provides @Singleton
    public ProductDao provideProductDao(AppDatabase database) { return database.productDao(); }
}
