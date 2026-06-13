package vn.vuavuive.customer;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.Gson;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import vn.vuavuive.customer.data.repository.AuthRepository;
import vn.vuavuive.customer.data.repository.CartRepository;
import vn.vuavuive.customer.data.repository.OrderRepository;
import vn.vuavuive.customer.data.repository.ProductRepository;
import vn.vuavuive.customer.data.repository.ShipmentRepository;
import vn.vuavuive.customer.di.DatabaseModule;
import vn.vuavuive.customer.di.DatabaseModule_ProvideCartDaoFactory;
import vn.vuavuive.customer.di.DatabaseModule_ProvideDatabaseFactory;
import vn.vuavuive.customer.di.DatabaseModule_ProvideProductDaoFactory;
import vn.vuavuive.customer.di.NetworkModule;
import vn.vuavuive.customer.di.NetworkModule_ProvideAuthApiFactory;
import vn.vuavuive.customer.di.NetworkModule_ProvideCartApiFactory;
import vn.vuavuive.customer.di.NetworkModule_ProvideChatbotApiFactory;
import vn.vuavuive.customer.di.NetworkModule_ProvideCookieJarFactory;
import vn.vuavuive.customer.di.NetworkModule_ProvideGsonFactory;
import vn.vuavuive.customer.di.NetworkModule_ProvideOkHttpClientFactory;
import vn.vuavuive.customer.di.NetworkModule_ProvideOrderApiFactory;
import vn.vuavuive.customer.di.NetworkModule_ProvidePaymentApiFactory;
import vn.vuavuive.customer.di.NetworkModule_ProvideProductApiFactory;
import vn.vuavuive.customer.di.NetworkModule_ProvideRecipeApiFactory;
import vn.vuavuive.customer.di.NetworkModule_ProvideRecommendApiFactory;
import vn.vuavuive.customer.di.NetworkModule_ProvideRetrofitFactory;
import vn.vuavuive.customer.di.NetworkModule_ProvideSessionManagerFactory;
import vn.vuavuive.customer.di.NetworkModule_ProvideShipmentApiFactory;
import vn.vuavuive.customer.di.NetworkModule_ProvideShipperOrderApiFactory;
import vn.vuavuive.customer.ui.MainActivity;
import vn.vuavuive.customer.ui.account.AccountFragment;
import vn.vuavuive.customer.ui.account.ChangePasswordActivity;
import vn.vuavuive.customer.ui.account.EditProfileActivity;
import vn.vuavuive.customer.ui.auth.ForgotPasswordActivity;
import vn.vuavuive.customer.ui.auth.LoginActivity;
import vn.vuavuive.customer.ui.auth.LoginActivity_MembersInjector;
import vn.vuavuive.customer.ui.auth.RegisterActivity;
import vn.vuavuive.customer.ui.cart.CartFragment;
import vn.vuavuive.customer.ui.chat.ChatActivity;
import vn.vuavuive.customer.ui.checkout.CheckoutActivity;
import vn.vuavuive.customer.ui.checkout.PaymentWebViewActivity;
import vn.vuavuive.customer.ui.home.HomeFragment;
import vn.vuavuive.customer.ui.order.OrderDetailActivity;
import vn.vuavuive.customer.ui.order.OrderListFragment;
import vn.vuavuive.customer.ui.product.ProductDetailActivity;
import vn.vuavuive.customer.ui.product.ProductListFragment;
import vn.vuavuive.customer.ui.recipe.RecipeDetailActivity;
import vn.vuavuive.customer.ui.recipe.RecipeListFragment;
import vn.vuavuive.customer.ui.recipe.RecipeListFragmentActivity;
import vn.vuavuive.customer.ui.review.MyReviewsActivity;
import vn.vuavuive.customer.ui.search.SearchActivity;
import vn.vuavuive.customer.ui.shipment.ShipmentDetailActivity;
import vn.vuavuive.customer.ui.shipment.ShipmentListActivity;
import vn.vuavuive.customer.ui.shipper.ShipperMainActivity;
import vn.vuavuive.customer.ui.shipper.ShipperMainActivity_MembersInjector;
import vn.vuavuive.customer.ui.shipper.ShipperOrderDetailActivity;
import vn.vuavuive.customer.ui.shipper.ShipperOrderDetailActivity_MembersInjector;
import vn.vuavuive.customer.ui.shipper.ShipperOrderListFragment;
import vn.vuavuive.customer.ui.shipper.ShipperOrderListFragment_MembersInjector;
import vn.vuavuive.customer.viewmodel.AuthViewModel;
import vn.vuavuive.customer.viewmodel.AuthViewModel_HiltModules;
import vn.vuavuive.customer.viewmodel.AuthViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import vn.vuavuive.customer.viewmodel.AuthViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import vn.vuavuive.customer.viewmodel.CartViewModel;
import vn.vuavuive.customer.viewmodel.CartViewModel_HiltModules;
import vn.vuavuive.customer.viewmodel.CartViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import vn.vuavuive.customer.viewmodel.CartViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import vn.vuavuive.customer.viewmodel.ChatViewModel;
import vn.vuavuive.customer.viewmodel.ChatViewModel_HiltModules;
import vn.vuavuive.customer.viewmodel.ChatViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import vn.vuavuive.customer.viewmodel.ChatViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import vn.vuavuive.customer.viewmodel.OrderViewModel;
import vn.vuavuive.customer.viewmodel.OrderViewModel_HiltModules;
import vn.vuavuive.customer.viewmodel.OrderViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import vn.vuavuive.customer.viewmodel.OrderViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import vn.vuavuive.customer.viewmodel.ProductViewModel;
import vn.vuavuive.customer.viewmodel.ProductViewModel_HiltModules;
import vn.vuavuive.customer.viewmodel.ProductViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import vn.vuavuive.customer.viewmodel.ProductViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import vn.vuavuive.customer.viewmodel.RecipeViewModel;
import vn.vuavuive.customer.viewmodel.RecipeViewModel_HiltModules;
import vn.vuavuive.customer.viewmodel.RecipeViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import vn.vuavuive.customer.viewmodel.RecipeViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import vn.vuavuive.customer.viewmodel.ShipmentViewModel;
import vn.vuavuive.customer.viewmodel.ShipmentViewModel_HiltModules;
import vn.vuavuive.customer.viewmodel.ShipmentViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import vn.vuavuive.customer.viewmodel.ShipmentViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import vn.vuavuive.shared.data.api.AuthApi;
import vn.vuavuive.shared.data.api.CartApi;
import vn.vuavuive.shared.data.api.ChatbotApi;
import vn.vuavuive.shared.data.api.OrderApi;
import vn.vuavuive.shared.data.api.PaymentApi;
import vn.vuavuive.shared.data.api.ProductApi;
import vn.vuavuive.shared.data.api.RecipeApi;
import vn.vuavuive.shared.data.api.RecommendApi;
import vn.vuavuive.shared.data.api.ShipmentApi;
import vn.vuavuive.shared.data.api.ShipperOrderApi;
import vn.vuavuive.shared.data.local.AppDatabase;
import vn.vuavuive.shared.data.local.CartDao;
import vn.vuavuive.shared.data.local.ProductDao;
import vn.vuavuive.shared.util.PersistentCookieJar;
import vn.vuavuive.shared.util.SessionManager;

@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class DaggerVuaVuiVeApp_HiltComponents_SingletonC {
  private DaggerVuaVuiVeApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private DatabaseModule databaseModule;

    private NetworkModule networkModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public Builder databaseModule(DatabaseModule databaseModule) {
      this.databaseModule = Preconditions.checkNotNull(databaseModule);
      return this;
    }

    public Builder networkModule(NetworkModule networkModule) {
      this.networkModule = Preconditions.checkNotNull(networkModule);
      return this;
    }

    public VuaVuiVeApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      if (databaseModule == null) {
        this.databaseModule = new DatabaseModule();
      }
      if (networkModule == null) {
        this.networkModule = new NetworkModule();
      }
      return new SingletonCImpl(applicationContextModule, databaseModule, networkModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements VuaVuiVeApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public VuaVuiVeApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements VuaVuiVeApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public VuaVuiVeApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements VuaVuiVeApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public VuaVuiVeApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements VuaVuiVeApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public VuaVuiVeApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements VuaVuiVeApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public VuaVuiVeApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements VuaVuiVeApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public VuaVuiVeApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements VuaVuiVeApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public VuaVuiVeApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends VuaVuiVeApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends VuaVuiVeApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    FragmentCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }

    @Override
    public void injectAccountFragment(AccountFragment arg0) {
    }

    @Override
    public void injectCartFragment(CartFragment arg0) {
    }

    @Override
    public void injectHomeFragment(HomeFragment arg0) {
    }

    @Override
    public void injectOrderListFragment(OrderListFragment arg0) {
    }

    @Override
    public void injectProductListFragment(ProductListFragment arg0) {
    }

    @Override
    public void injectRecipeListFragment(RecipeListFragment arg0) {
    }

    @Override
    public void injectShipperOrderListFragment(ShipperOrderListFragment arg0) {
      injectShipperOrderListFragment2(arg0);
    }

    @CanIgnoreReturnValue
    private ShipperOrderListFragment injectShipperOrderListFragment2(
        ShipperOrderListFragment instance) {
      ShipperOrderListFragment_MembersInjector.injectShipperOrderApi(instance, singletonCImpl.provideShipperOrderApiProvider.get());
      return instance;
    }
  }

  private static final class ViewCImpl extends VuaVuiVeApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends VuaVuiVeApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    ActivityCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(7).put(AuthViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AuthViewModel_HiltModules.KeyModule.provide()).put(CartViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, CartViewModel_HiltModules.KeyModule.provide()).put(ChatViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ChatViewModel_HiltModules.KeyModule.provide()).put(OrderViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, OrderViewModel_HiltModules.KeyModule.provide()).put(ProductViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ProductViewModel_HiltModules.KeyModule.provide()).put(RecipeViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, RecipeViewModel_HiltModules.KeyModule.provide()).put(ShipmentViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ShipmentViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public void injectMainActivity(MainActivity arg0) {
    }

    @Override
    public void injectChangePasswordActivity(ChangePasswordActivity arg0) {
    }

    @Override
    public void injectEditProfileActivity(EditProfileActivity arg0) {
    }

    @Override
    public void injectForgotPasswordActivity(ForgotPasswordActivity arg0) {
    }

    @Override
    public void injectLoginActivity(LoginActivity arg0) {
      injectLoginActivity2(arg0);
    }

    @Override
    public void injectRegisterActivity(RegisterActivity arg0) {
    }

    @Override
    public void injectChatActivity(ChatActivity arg0) {
    }

    @Override
    public void injectCheckoutActivity(CheckoutActivity arg0) {
    }

    @Override
    public void injectPaymentWebViewActivity(PaymentWebViewActivity arg0) {
    }

    @Override
    public void injectOrderDetailActivity(OrderDetailActivity arg0) {
    }

    @Override
    public void injectProductDetailActivity(ProductDetailActivity arg0) {
    }

    @Override
    public void injectRecipeDetailActivity(RecipeDetailActivity arg0) {
    }

    @Override
    public void injectRecipeListFragmentActivity(RecipeListFragmentActivity arg0) {
    }

    @Override
    public void injectMyReviewsActivity(MyReviewsActivity arg0) {
    }

    @Override
    public void injectSearchActivity(SearchActivity arg0) {
    }

    @Override
    public void injectShipmentDetailActivity(ShipmentDetailActivity arg0) {
    }

    @Override
    public void injectShipmentListActivity(ShipmentListActivity arg0) {
    }

    @Override
    public void injectShipperMainActivity(ShipperMainActivity arg0) {
      injectShipperMainActivity2(arg0);
    }

    @Override
    public void injectShipperOrderDetailActivity(ShipperOrderDetailActivity arg0) {
      injectShipperOrderDetailActivity2(arg0);
    }

    @CanIgnoreReturnValue
    private LoginActivity injectLoginActivity2(LoginActivity instance) {
      LoginActivity_MembersInjector.injectSessionManager(instance, singletonCImpl.provideSessionManagerProvider.get());
      return instance;
    }

    @CanIgnoreReturnValue
    private ShipperMainActivity injectShipperMainActivity2(ShipperMainActivity instance2) {
      ShipperMainActivity_MembersInjector.injectSessionManager(instance2, singletonCImpl.provideSessionManagerProvider.get());
      ShipperMainActivity_MembersInjector.injectShipperOrderApi(instance2, singletonCImpl.provideShipperOrderApiProvider.get());
      return instance2;
    }

    @CanIgnoreReturnValue
    private ShipperOrderDetailActivity injectShipperOrderDetailActivity2(
        ShipperOrderDetailActivity instance3) {
      ShipperOrderDetailActivity_MembersInjector.injectShipperOrderApi(instance3, singletonCImpl.provideShipperOrderApiProvider.get());
      ShipperOrderDetailActivity_MembersInjector.injectOrderApi(instance3, singletonCImpl.provideOrderApiProvider.get());
      ShipperOrderDetailActivity_MembersInjector.injectSessionManager(instance3, singletonCImpl.provideSessionManagerProvider.get());
      return instance3;
    }
  }

  private static final class ViewModelCImpl extends VuaVuiVeApp_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    Provider<AuthViewModel> authViewModelProvider;

    Provider<CartViewModel> cartViewModelProvider;

    Provider<ChatViewModel> chatViewModelProvider;

    Provider<OrderViewModel> orderViewModelProvider;

    Provider<ProductViewModel> productViewModelProvider;

    Provider<RecipeViewModel> recipeViewModelProvider;

    Provider<ShipmentViewModel> shipmentViewModelProvider;

    ViewModelCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        SavedStateHandle savedStateHandleParam, ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.authViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.cartViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.chatViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.orderViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.productViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.recipeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.shipmentViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(7).put(AuthViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (authViewModelProvider))).put(CartViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (cartViewModelProvider))).put(ChatViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (chatViewModelProvider))).put(OrderViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (orderViewModelProvider))).put(ProductViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (productViewModelProvider))).put(RecipeViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (recipeViewModelProvider))).put(ShipmentViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (shipmentViewModelProvider))).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // vn.vuavuive.customer.viewmodel.AuthViewModel
          return (T) new AuthViewModel(singletonCImpl.authRepositoryProvider.get());

          case 1: // vn.vuavuive.customer.viewmodel.CartViewModel
          return (T) new CartViewModel(singletonCImpl.cartRepositoryProvider.get());

          case 2: // vn.vuavuive.customer.viewmodel.ChatViewModel
          return (T) new ChatViewModel(singletonCImpl.provideChatbotApiProvider.get());

          case 3: // vn.vuavuive.customer.viewmodel.OrderViewModel
          return (T) new OrderViewModel(singletonCImpl.orderRepositoryProvider.get());

          case 4: // vn.vuavuive.customer.viewmodel.ProductViewModel
          return (T) new ProductViewModel(singletonCImpl.productRepositoryProvider.get());

          case 5: // vn.vuavuive.customer.viewmodel.RecipeViewModel
          return (T) new RecipeViewModel(singletonCImpl.provideRecipeApiProvider.get());

          case 6: // vn.vuavuive.customer.viewmodel.ShipmentViewModel
          return (T) new ShipmentViewModel(singletonCImpl.shipmentRepositoryProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends VuaVuiVeApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends VuaVuiVeApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends VuaVuiVeApp_HiltComponents.SingletonC {
    private final NetworkModule networkModule;

    private final ApplicationContextModule applicationContextModule;

    private final DatabaseModule databaseModule;

    private final SingletonCImpl singletonCImpl = this;

    Provider<SessionManager> provideSessionManagerProvider;

    Provider<PersistentCookieJar> provideCookieJarProvider;

    Provider<OkHttpClient> provideOkHttpClientProvider;

    Provider<Gson> provideGsonProvider;

    Provider<Retrofit> provideRetrofitProvider;

    Provider<ShipperOrderApi> provideShipperOrderApiProvider;

    Provider<OrderApi> provideOrderApiProvider;

    Provider<AuthApi> provideAuthApiProvider;

    Provider<AuthRepository> authRepositoryProvider;

    Provider<CartApi> provideCartApiProvider;

    Provider<AppDatabase> provideDatabaseProvider;

    Provider<CartDao> provideCartDaoProvider;

    Provider<CartRepository> cartRepositoryProvider;

    Provider<ChatbotApi> provideChatbotApiProvider;

    Provider<PaymentApi> providePaymentApiProvider;

    Provider<OrderRepository> orderRepositoryProvider;

    Provider<ProductApi> provideProductApiProvider;

    Provider<RecommendApi> provideRecommendApiProvider;

    Provider<ProductDao> provideProductDaoProvider;

    Provider<ProductRepository> productRepositoryProvider;

    Provider<RecipeApi> provideRecipeApiProvider;

    Provider<ShipmentApi> provideShipmentApiProvider;

    Provider<ShipmentRepository> shipmentRepositoryProvider;

    SingletonCImpl(ApplicationContextModule applicationContextModuleParam,
        DatabaseModule databaseModuleParam, NetworkModule networkModuleParam) {
      this.networkModule = networkModuleParam;
      this.applicationContextModule = applicationContextModuleParam;
      this.databaseModule = databaseModuleParam;
      initialize(applicationContextModuleParam, databaseModuleParam, networkModuleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam,
        final DatabaseModule databaseModuleParam, final NetworkModule networkModuleParam) {
      this.provideSessionManagerProvider = DoubleCheck.provider(new SwitchingProvider<SessionManager>(singletonCImpl, 0));
      this.provideCookieJarProvider = DoubleCheck.provider(new SwitchingProvider<PersistentCookieJar>(singletonCImpl, 4));
      this.provideOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 3));
      this.provideGsonProvider = DoubleCheck.provider(new SwitchingProvider<Gson>(singletonCImpl, 5));
      this.provideRetrofitProvider = DoubleCheck.provider(new SwitchingProvider<Retrofit>(singletonCImpl, 2));
      this.provideShipperOrderApiProvider = DoubleCheck.provider(new SwitchingProvider<ShipperOrderApi>(singletonCImpl, 1));
      this.provideOrderApiProvider = DoubleCheck.provider(new SwitchingProvider<OrderApi>(singletonCImpl, 6));
      this.provideAuthApiProvider = DoubleCheck.provider(new SwitchingProvider<AuthApi>(singletonCImpl, 8));
      this.authRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<AuthRepository>(singletonCImpl, 7));
      this.provideCartApiProvider = DoubleCheck.provider(new SwitchingProvider<CartApi>(singletonCImpl, 10));
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<AppDatabase>(singletonCImpl, 12));
      this.provideCartDaoProvider = DoubleCheck.provider(new SwitchingProvider<CartDao>(singletonCImpl, 11));
      this.cartRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<CartRepository>(singletonCImpl, 9));
      this.provideChatbotApiProvider = DoubleCheck.provider(new SwitchingProvider<ChatbotApi>(singletonCImpl, 13));
      this.providePaymentApiProvider = DoubleCheck.provider(new SwitchingProvider<PaymentApi>(singletonCImpl, 15));
      this.orderRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<OrderRepository>(singletonCImpl, 14));
      this.provideProductApiProvider = DoubleCheck.provider(new SwitchingProvider<ProductApi>(singletonCImpl, 17));
      this.provideRecommendApiProvider = DoubleCheck.provider(new SwitchingProvider<RecommendApi>(singletonCImpl, 18));
      this.provideProductDaoProvider = DoubleCheck.provider(new SwitchingProvider<ProductDao>(singletonCImpl, 19));
      this.productRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ProductRepository>(singletonCImpl, 16));
      this.provideRecipeApiProvider = DoubleCheck.provider(new SwitchingProvider<RecipeApi>(singletonCImpl, 20));
      this.provideShipmentApiProvider = DoubleCheck.provider(new SwitchingProvider<ShipmentApi>(singletonCImpl, 22));
      this.shipmentRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ShipmentRepository>(singletonCImpl, 21));
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    @Override
    public void injectVuaVuiVeApp(VuaVuiVeApp arg0) {
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // vn.vuavuive.shared.util.SessionManager
          return (T) NetworkModule_ProvideSessionManagerFactory.provideSessionManager(singletonCImpl.networkModule, ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 1: // vn.vuavuive.shared.data.api.ShipperOrderApi
          return (T) NetworkModule_ProvideShipperOrderApiFactory.provideShipperOrderApi(singletonCImpl.networkModule, singletonCImpl.provideRetrofitProvider.get());

          case 2: // retrofit2.Retrofit
          return (T) NetworkModule_ProvideRetrofitFactory.provideRetrofit(singletonCImpl.networkModule, singletonCImpl.provideOkHttpClientProvider.get(), singletonCImpl.provideGsonProvider.get());

          case 3: // okhttp3.OkHttpClient
          return (T) NetworkModule_ProvideOkHttpClientFactory.provideOkHttpClient(singletonCImpl.networkModule, singletonCImpl.provideCookieJarProvider.get());

          case 4: // vn.vuavuive.shared.util.PersistentCookieJar
          return (T) NetworkModule_ProvideCookieJarFactory.provideCookieJar(singletonCImpl.networkModule, ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 5: // com.google.gson.Gson
          return (T) NetworkModule_ProvideGsonFactory.provideGson(singletonCImpl.networkModule);

          case 6: // vn.vuavuive.shared.data.api.OrderApi
          return (T) NetworkModule_ProvideOrderApiFactory.provideOrderApi(singletonCImpl.networkModule, singletonCImpl.provideRetrofitProvider.get());

          case 7: // vn.vuavuive.customer.data.repository.AuthRepository
          return (T) new AuthRepository(singletonCImpl.provideAuthApiProvider.get(), singletonCImpl.provideSessionManagerProvider.get());

          case 8: // vn.vuavuive.shared.data.api.AuthApi
          return (T) NetworkModule_ProvideAuthApiFactory.provideAuthApi(singletonCImpl.networkModule, singletonCImpl.provideRetrofitProvider.get());

          case 9: // vn.vuavuive.customer.data.repository.CartRepository
          return (T) new CartRepository(singletonCImpl.provideCartApiProvider.get(), singletonCImpl.provideCartDaoProvider.get(), singletonCImpl.provideSessionManagerProvider.get());

          case 10: // vn.vuavuive.shared.data.api.CartApi
          return (T) NetworkModule_ProvideCartApiFactory.provideCartApi(singletonCImpl.networkModule, singletonCImpl.provideRetrofitProvider.get());

          case 11: // vn.vuavuive.shared.data.local.CartDao
          return (T) DatabaseModule_ProvideCartDaoFactory.provideCartDao(singletonCImpl.databaseModule, singletonCImpl.provideDatabaseProvider.get());

          case 12: // vn.vuavuive.shared.data.local.AppDatabase
          return (T) DatabaseModule_ProvideDatabaseFactory.provideDatabase(singletonCImpl.databaseModule, ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 13: // vn.vuavuive.shared.data.api.ChatbotApi
          return (T) NetworkModule_ProvideChatbotApiFactory.provideChatbotApi(singletonCImpl.networkModule, singletonCImpl.provideRetrofitProvider.get());

          case 14: // vn.vuavuive.customer.data.repository.OrderRepository
          return (T) new OrderRepository(singletonCImpl.provideOrderApiProvider.get(), singletonCImpl.providePaymentApiProvider.get());

          case 15: // vn.vuavuive.shared.data.api.PaymentApi
          return (T) NetworkModule_ProvidePaymentApiFactory.providePaymentApi(singletonCImpl.networkModule, singletonCImpl.provideRetrofitProvider.get());

          case 16: // vn.vuavuive.customer.data.repository.ProductRepository
          return (T) new ProductRepository(singletonCImpl.provideProductApiProvider.get(), singletonCImpl.provideRecommendApiProvider.get(), singletonCImpl.provideProductDaoProvider.get());

          case 17: // vn.vuavuive.shared.data.api.ProductApi
          return (T) NetworkModule_ProvideProductApiFactory.provideProductApi(singletonCImpl.networkModule, singletonCImpl.provideRetrofitProvider.get());

          case 18: // vn.vuavuive.shared.data.api.RecommendApi
          return (T) NetworkModule_ProvideRecommendApiFactory.provideRecommendApi(singletonCImpl.networkModule, singletonCImpl.provideRetrofitProvider.get());

          case 19: // vn.vuavuive.shared.data.local.ProductDao
          return (T) DatabaseModule_ProvideProductDaoFactory.provideProductDao(singletonCImpl.databaseModule, singletonCImpl.provideDatabaseProvider.get());

          case 20: // vn.vuavuive.shared.data.api.RecipeApi
          return (T) NetworkModule_ProvideRecipeApiFactory.provideRecipeApi(singletonCImpl.networkModule, singletonCImpl.provideRetrofitProvider.get());

          case 21: // vn.vuavuive.customer.data.repository.ShipmentRepository
          return (T) new ShipmentRepository(singletonCImpl.provideShipmentApiProvider.get());

          case 22: // vn.vuavuive.shared.data.api.ShipmentApi
          return (T) NetworkModule_ProvideShipmentApiFactory.provideShipmentApi(singletonCImpl.networkModule, singletonCImpl.provideRetrofitProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
