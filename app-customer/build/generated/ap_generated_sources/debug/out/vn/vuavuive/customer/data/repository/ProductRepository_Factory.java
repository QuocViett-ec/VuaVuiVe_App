package vn.vuavuive.customer.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import vn.vuavuive.shared.data.api.ProductApi;
import vn.vuavuive.shared.data.api.RecommendApi;
import vn.vuavuive.shared.data.local.ProductDao;

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
public final class ProductRepository_Factory implements Factory<ProductRepository> {
  private final Provider<ProductApi> productApiProvider;

  private final Provider<RecommendApi> recommendApiProvider;

  private final Provider<ProductDao> productDaoProvider;

  public ProductRepository_Factory(Provider<ProductApi> productApiProvider,
      Provider<RecommendApi> recommendApiProvider, Provider<ProductDao> productDaoProvider) {
    this.productApiProvider = productApiProvider;
    this.recommendApiProvider = recommendApiProvider;
    this.productDaoProvider = productDaoProvider;
  }

  @Override
  public ProductRepository get() {
    return newInstance(productApiProvider.get(), recommendApiProvider.get(), productDaoProvider.get());
  }

  public static ProductRepository_Factory create(Provider<ProductApi> productApiProvider,
      Provider<RecommendApi> recommendApiProvider, Provider<ProductDao> productDaoProvider) {
    return new ProductRepository_Factory(productApiProvider, recommendApiProvider, productDaoProvider);
  }

  public static ProductRepository newInstance(ProductApi productApi, RecommendApi recommendApi,
      ProductDao productDao) {
    return new ProductRepository(productApi, recommendApi, productDao);
  }
}
