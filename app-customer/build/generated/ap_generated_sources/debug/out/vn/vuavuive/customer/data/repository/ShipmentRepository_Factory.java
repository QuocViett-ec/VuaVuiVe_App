package vn.vuavuive.customer.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import vn.vuavuive.shared.data.api.ShipmentApi;

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
public final class ShipmentRepository_Factory implements Factory<ShipmentRepository> {
  private final Provider<ShipmentApi> shipmentApiProvider;

  public ShipmentRepository_Factory(Provider<ShipmentApi> shipmentApiProvider) {
    this.shipmentApiProvider = shipmentApiProvider;
  }

  @Override
  public ShipmentRepository get() {
    return newInstance(shipmentApiProvider.get());
  }

  public static ShipmentRepository_Factory create(Provider<ShipmentApi> shipmentApiProvider) {
    return new ShipmentRepository_Factory(shipmentApiProvider);
  }

  public static ShipmentRepository newInstance(ShipmentApi shipmentApi) {
    return new ShipmentRepository(shipmentApi);
  }
}
