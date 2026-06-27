package vn.vuavuive.shipper.di;

import android.content.Context;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;
import vn.vuavuive.shared.util.SessionManager;
import vn.vuavuive.shipper.data.repository.FirebaseShipperRepository;

/**
 * ShipperModule — Hilt DI module cho app-shipper (Firebase-only).
 *
 * Thay thế NetworkModule cũ (Retrofit-based).
 * Cung cấp:
 * - SessionManager: lưu/đọc session local (SharedPreferences)
 * - FirebaseShipperRepository: nguồn dữ liệu duy nhất (Firebase Auth + RTDB)
 */
@Module
@InstallIn(SingletonComponent.class)
public class ShipperModule {

    @Provides
    @Singleton
    public SessionManager provideSessionManager(@ApplicationContext Context context) {
        return new SessionManager(context);
    }

    @Provides
    @Singleton
    public FirebaseShipperRepository provideFirebaseShipperRepository(SessionManager sessionManager) {
        return new FirebaseShipperRepository(sessionManager);
    }
}
