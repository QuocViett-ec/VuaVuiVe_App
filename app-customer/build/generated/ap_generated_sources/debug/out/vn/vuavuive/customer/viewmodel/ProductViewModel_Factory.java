package vn.vuavuive.customer.viewmodel;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import vn.vuavuive.customer.data.repository.ProductRepository;

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
public final class ProductViewModel_Factory implements Factory<ProductViewModel> {
  private final Provider<ProductRepository> productRepositoryProvider;

  public ProductViewModel_Factory(Provider<ProductRepository> productRepositoryProvider) {
    this.productRepositoryProvider = productRepositoryProvider;
  }

  @Override
  public ProductViewModel get() {
    return newInstance(productRepositoryProvider.get());
  }

  public static ProductViewModel_Factory create(
      Provider<ProductRepository> productRepositoryProvider) {
    return new ProductViewModel_Factory(productRepositoryProvider);
  }

  public static ProductViewModel newInstance(ProductRepository productRepository) {
    return new ProductViewModel(productRepository);
  }
}
