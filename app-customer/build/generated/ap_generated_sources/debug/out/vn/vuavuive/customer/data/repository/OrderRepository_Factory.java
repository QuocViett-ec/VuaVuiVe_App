package vn.vuavuive.customer.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import vn.vuavuive.shared.data.api.OrderApi;
import vn.vuavuive.shared.data.api.PaymentApi;

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
public final class OrderRepository_Factory implements Factory<OrderRepository> {
  private final Provider<OrderApi> orderApiProvider;

  private final Provider<PaymentApi> paymentApiProvider;

  public OrderRepository_Factory(Provider<OrderApi> orderApiProvider,
      Provider<PaymentApi> paymentApiProvider) {
    this.orderApiProvider = orderApiProvider;
    this.paymentApiProvider = paymentApiProvider;
  }

  @Override
  public OrderRepository get() {
    return newInstance(orderApiProvider.get(), paymentApiProvider.get());
  }

  public static OrderRepository_Factory create(Provider<OrderApi> orderApiProvider,
      Provider<PaymentApi> paymentApiProvider) {
    return new OrderRepository_Factory(orderApiProvider, paymentApiProvider);
  }

  public static OrderRepository newInstance(OrderApi orderApi, PaymentApi paymentApi) {
    return new OrderRepository(orderApi, paymentApi);
  }
}
