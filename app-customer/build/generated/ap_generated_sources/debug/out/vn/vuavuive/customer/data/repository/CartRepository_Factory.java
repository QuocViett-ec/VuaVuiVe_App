package vn.vuavuive.customer.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import vn.vuavuive.shared.data.api.CartApi;
import vn.vuavuive.shared.data.local.CartDao;
import vn.vuavuive.shared.util.SessionManager;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
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
public final class CartRepository_Factory implements Factory<CartRepository> {
  private final Provider<CartApi> cartApiProvider;

  private final Provider<CartDao> cartDaoProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  public CartRepository_Factory(Provider<CartApi> cartApiProvider,
      Provider<CartDao> cartDaoProvider, Provider<SessionManager> sessionManagerProvider) {
    this.cartApiProvider = cartApiProvider;
    this.cartDaoProvider = cartDaoProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public CartRepository get() {
    return newInstance(cartApiProvider.get(), cartDaoProvider.get(), sessionManagerProvider.get());
  }

  public static CartRepository_Factory create(Provider<CartApi> cartApiProvider,
      Provider<CartDao> cartDaoProvider, Provider<SessionManager> sessionManagerProvider) {
    return new CartRepository_Factory(cartApiProvider, cartDaoProvider, sessionManagerProvider);
  }

  public static CartRepository newInstance(CartApi cartApi, CartDao cartDao,
      SessionManager sessionManager) {
    return new CartRepository(cartApi, cartDao, sessionManager);
  }
}
