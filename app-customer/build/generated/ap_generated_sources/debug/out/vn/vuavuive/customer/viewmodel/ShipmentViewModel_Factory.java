package vn.vuavuive.customer.viewmodel;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import vn.vuavuive.customer.data.repository.ShipmentRepository;

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
public final class ShipmentViewModel_Factory implements Factory<ShipmentViewModel> {
  private final Provider<ShipmentRepository> shipmentRepositoryProvider;

  public ShipmentViewModel_Factory(Provider<ShipmentRepository> shipmentRepositoryProvider) {
    this.shipmentRepositoryProvider = shipmentRepositoryProvider;
  }

  @Override
  public ShipmentViewModel get() {
    return newInstance(shipmentRepositoryProvider.get());
  }

  public static ShipmentViewModel_Factory create(
      Provider<ShipmentRepository> shipmentRepositoryProvider) {
    return new ShipmentViewModel_Factory(shipmentRepositoryProvider);
  }

  public static ShipmentViewModel newInstance(ShipmentRepository shipmentRepository) {
    return new ShipmentViewModel(shipmentRepository);
  }
}
