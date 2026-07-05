package vn.vuavuive.admin.di;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.vuavuive.admin.BuildConfig;
import vn.vuavuive.admin.data.firebase.FirebaseAuthApi;
import vn.vuavuive.admin.data.firebase.FirebaseProductApi;
import vn.vuavuive.admin.data.firebase.FirebaseOrderApi;
import vn.vuavuive.admin.data.firebase.FirebaseAdminProductApi;
import vn.vuavuive.admin.data.firebase.FirebaseAdminOrderApi;
import vn.vuavuive.admin.data.firebase.FirebaseShipmentApi;
import vn.vuavuive.admin.data.firebase.FirebaseAdminUserApi;
import vn.vuavuive.admin.data.firebase.FirebaseAdminVoucherApi;
import vn.vuavuive.admin.data.firebase.FirebaseAdminShipmentApi;
import vn.vuavuive.admin.data.firebase.FirebaseDashboardApi;
import vn.vuavuive.admin.data.firebase.FirebaseAuditLogApi;
import vn.vuavuive.shared.data.api.AdminChatbotApi;
import vn.vuavuive.shared.data.api.AdminOrderApi;
import vn.vuavuive.shared.data.api.AdminProductApi;
import vn.vuavuive.shared.data.api.AdminShipmentApi;
import vn.vuavuive.shared.data.api.AdminUserApi;
import vn.vuavuive.shared.data.api.AdminVoucherApi;
import vn.vuavuive.shared.data.api.AuditLogApi;
import vn.vuavuive.shared.data.api.AuthApi;
import vn.vuavuive.shared.data.api.DashboardApi;
import vn.vuavuive.shared.data.api.OrderApi;
import vn.vuavuive.shared.data.api.OrderStatusApi;
import vn.vuavuive.shared.data.api.ProductApi;
import vn.vuavuive.shared.data.api.ShipmentApi;
import vn.vuavuive.shared.util.AuthInterceptor;
import vn.vuavuive.shared.util.CsrfInterceptor;
import vn.vuavuive.shared.util.PersistentCookieJar;
import vn.vuavuive.shared.util.PortalScopeInterceptor;
import vn.vuavuive.shared.util.SessionManager;
import java.util.concurrent.TimeUnit;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    @Provides
    @Singleton
    public PersistentCookieJar provideCookieJar(@ApplicationContext Context context) {
        return new PersistentCookieJar(context);
    }

    @Provides
    @Singleton
    public SessionManager provideSessionManager(@ApplicationContext Context context) {
        return new SessionManager(context);
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(PersistentCookieJar cookieJar, SessionManager sessionManager) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.NONE);

        return new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .addInterceptor(new AuthInterceptor(sessionManager))   // << JWT token tự động
                .addInterceptor(new PortalScopeInterceptor(BuildConfig.PORTAL_SCOPE))
                .addInterceptor(new CsrfInterceptor())
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
    public AuthApi provideAuthApi(Retrofit retrofit) { return new FirebaseAuthApi(); }

    @Provides @Singleton
    public ProductApi provideProductApi(Retrofit retrofit) { return new FirebaseProductApi(); }

    @Provides @Singleton
    public OrderApi provideOrderApi(Retrofit retrofit) { return new FirebaseOrderApi(); }

    @Provides @Singleton
    public ShipmentApi provideShipmentApi(Retrofit retrofit) { return new FirebaseShipmentApi(); }

    @Provides @Singleton
    public AdminOrderApi provideAdminOrderApi(Retrofit retrofit) { return new FirebaseAdminOrderApi(); }

    @Provides @Singleton
    public OrderStatusApi provideOrderStatusApi(Retrofit retrofit) {
        return retrofit.create(OrderStatusApi.class);
    }

    @Provides @Singleton
    public AdminProductApi provideAdminProductApi(Retrofit retrofit) { return new FirebaseAdminProductApi(); }

    @Provides @Singleton
    public AdminUserApi provideAdminUserApi(Retrofit retrofit) { return new FirebaseAdminUserApi(); }

    @Provides @Singleton
    public AdminVoucherApi provideAdminVoucherApi(Retrofit retrofit) { return new FirebaseAdminVoucherApi(); }

    @Provides @Singleton
    public AdminShipmentApi provideAdminShipmentApi(Retrofit retrofit) { return new FirebaseAdminShipmentApi(); }

    @Provides @Singleton
    public AdminChatbotApi provideAdminChatbotApi(Retrofit retrofit) { return retrofit.create(AdminChatbotApi.class); }

    @Provides @Singleton
    public DashboardApi provideDashboardApi(Retrofit retrofit) { return new FirebaseDashboardApi(); }

    @Provides @Singleton
    public AuditLogApi provideAuditLogApi(Retrofit retrofit) { return new FirebaseAuditLogApi(); }
}
