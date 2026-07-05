package vn.vuavuive.shipper.di;

import android.content.Context;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.vuavuive.shipper.BuildConfig;
import vn.vuavuive.shared.data.api.ShipperOrderApi;
import vn.vuavuive.shared.util.AuthInterceptor;
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
    public OkHttpClient provideOkHttpClient(SessionManager sessionManager) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.NONE);
        return new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(sessionManager))
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL + "/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    public ShipperOrderApi provideShipperOrderApi(Retrofit retrofit) {
        return retrofit.create(ShipperOrderApi.class);
    }

    @Provides
    @Singleton
    public FirebaseShipperRepository provideFirebaseShipperRepository(
            SessionManager sessionManager,
            ShipperOrderApi shipperOrderApi) {
        return new FirebaseShipperRepository(sessionManager, shipperOrderApi);
    }
}
