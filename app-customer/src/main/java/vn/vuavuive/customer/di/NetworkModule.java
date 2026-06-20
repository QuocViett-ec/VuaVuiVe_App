package vn.vuavuive.customer.di;

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
import vn.vuavuive.customer.BuildConfig;
import vn.vuavuive.shared.data.api.AdminChatbotApi;
import vn.vuavuive.shared.data.api.AdminOrderApi;
import vn.vuavuive.shared.data.api.AdminProductApi;
import vn.vuavuive.shared.data.api.AdminShipmentApi;
import vn.vuavuive.shared.data.api.AdminUserApi;
import vn.vuavuive.shared.data.api.AdminVoucherApi;
import vn.vuavuive.shared.data.api.AuditLogApi;
import vn.vuavuive.shared.data.api.AuthApi;
import vn.vuavuive.shared.data.api.CartApi;
import vn.vuavuive.shared.data.api.CategoryApi;
import vn.vuavuive.shared.data.api.ChatbotApi;
import vn.vuavuive.shared.data.api.DashboardApi;
import vn.vuavuive.shared.data.api.OrderApi;
import vn.vuavuive.shared.data.api.PaymentApi;
import vn.vuavuive.shared.data.api.ProductApi;
import vn.vuavuive.shared.data.api.RecommendApi;
import vn.vuavuive.shared.data.api.RecipeApi;
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
    public AuthApi provideAuthApi(Retrofit retrofit) { return retrofit.create(AuthApi.class); }

    @Provides @Singleton
    public ProductApi provideProductApi(Retrofit retrofit) { return retrofit.create(ProductApi.class); }

    @Provides @Singleton
    public CartApi provideCartApi(Retrofit retrofit) { return retrofit.create(CartApi.class); }

    @Provides @Singleton
    public OrderApi provideOrderApi(Retrofit retrofit) { return retrofit.create(OrderApi.class); }

    @Provides @Singleton
    public PaymentApi providePaymentApi(Retrofit retrofit) { return retrofit.create(PaymentApi.class); }

    @Provides @Singleton
    public ShipmentApi provideShipmentApi(Retrofit retrofit) { return retrofit.create(ShipmentApi.class); }

    @Provides @Singleton
    public RecipeApi provideRecipeApi(Retrofit retrofit) { return retrofit.create(RecipeApi.class); }

    @Provides @Singleton
    public RecommendApi provideRecommendApi(Retrofit retrofit) { return retrofit.create(RecommendApi.class); }

    @Provides @Singleton
    public ChatbotApi provideChatbotApi(Retrofit retrofit) { return retrofit.create(ChatbotApi.class); }

    @Provides @Singleton
    public CategoryApi provideCategoryApi(Retrofit retrofit) { return retrofit.create(CategoryApi.class); }
}
