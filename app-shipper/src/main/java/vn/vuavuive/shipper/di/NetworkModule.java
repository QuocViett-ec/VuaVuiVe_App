package vn.vuavuive.shipper.di;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import java.util.concurrent.TimeUnit;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.vuavuive.shared.data.api.AuthApi;
import vn.vuavuive.shared.data.api.OrderApi;
import vn.vuavuive.shared.data.api.ShipperOrderApi;
import vn.vuavuive.shared.util.AuthInterceptor;
import vn.vuavuive.shared.util.PortalScopeInterceptor;
import vn.vuavuive.shared.util.SessionManager;
import vn.vuavuive.shipper.BuildConfig;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

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
                .addInterceptor(new PortalScopeInterceptor(BuildConfig.PORTAL_SCOPE))
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Provides
    @Singleton
    public Gson provideGson() {
        return new GsonBuilder().setLenient().serializeNulls().create();
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient okHttpClient, Gson gson) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL + "/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    @Provides @Singleton
    public AuthApi provideAuthApi(Retrofit retrofit) { return retrofit.create(AuthApi.class); }

    @Provides @Singleton
    public OrderApi provideOrderApi(Retrofit retrofit) { return retrofit.create(OrderApi.class); }

    @Provides @Singleton
    public ShipperOrderApi provideShipperOrderApi(Retrofit retrofit) { return retrofit.create(ShipperOrderApi.class); }
}
