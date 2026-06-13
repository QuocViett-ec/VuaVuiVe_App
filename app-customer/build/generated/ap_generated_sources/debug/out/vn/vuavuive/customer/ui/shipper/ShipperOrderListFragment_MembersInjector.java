package vn.vuavuive.customer.ui.shipper;

import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import vn.vuavuive.shared.data.api.ShipperOrderApi;

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
public final class ShipperOrderListFragment_MembersInjector implements MembersInjector<ShipperOrderListFragment> {
  private final Provider<ShipperOrderApi> shipperOrderApiProvider;

  public ShipperOrderListFragment_MembersInjector(
      Provider<ShipperOrderApi> shipperOrderApiProvider) {
    this.shipperOrderApiProvider = shipperOrderApiProvider;
  }

  public static MembersInjector<ShipperOrderListFragment> create(
      Provider<ShipperOrderApi> shipperOrderApiProvider) {
    return new ShipperOrderListFragment_MembersInjector(shipperOrderApiProvider);
  }

  @Override
  public void injectMembers(ShipperOrderListFragment instance) {
    injectShipperOrderApi(instance, shipperOrderApiProvider.get());
  }

  @InjectedFieldSignature("vn.vuavuive.customer.ui.shipper.ShipperOrderListFragment.shipperOrderApi")
  public static void injectShipperOrderApi(ShipperOrderListFragment instance,
      ShipperOrderApi shipperOrderApi) {
    instance.shipperOrderApi = shipperOrderApi;
  }
}
