package vn.vuavuive.customer.viewmodel;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import vn.vuavuive.customer.data.repository.OrderRepository;

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
public final class OrderViewModel_Factory implements Factory<OrderViewModel> {
  private final Provider<OrderRepository> orderRepositoryProvider;

  public OrderViewModel_Factory(Provider<OrderRepository> orderRepositoryProvider) {
    this.orderRepositoryProvider = orderRepositoryProvider;
  }

  @Override
  public OrderViewModel get() {
    return newInstance(orderRepositoryProvider.get());
  }

  public static OrderViewModel_Factory create(Provider<OrderRepository> orderRepositoryProvider) {
    return new OrderViewModel_Factory(orderRepositoryProvider);
  }

  public static OrderViewModel newInstance(OrderRepository orderRepository) {
    return new OrderViewModel(orderRepository);
  }
}
