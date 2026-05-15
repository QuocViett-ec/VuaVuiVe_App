package vn.vuavuive.customer.viewmodel;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import vn.vuavuive.customer.data.repository.CartRepository;

@ScopeMetadata
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
public final class CartViewModel_Factory implements Factory<CartViewModel> {
  private final Provider<CartRepository> cartRepositoryProvider;

  public CartViewModel_Factory(Provider<CartRepository> cartRepositoryProvider) {
    this.cartRepositoryProvider = cartRepositoryProvider;
  }

  @Override
  public CartViewModel get() {
    return newInstance(cartRepositoryProvider.get());
  }

  public static CartViewModel_Factory create(Provider<CartRepository> cartRepositoryProvider) {
    return new CartViewModel_Factory(cartRepositoryProvider);
  }

  public static CartViewModel newInstance(CartRepository cartRepository) {
    return new CartViewModel(cartRepository);
  }
}
