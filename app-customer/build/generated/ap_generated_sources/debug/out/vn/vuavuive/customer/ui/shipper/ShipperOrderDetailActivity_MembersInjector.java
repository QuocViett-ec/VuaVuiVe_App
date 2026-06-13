package vn.vuavuive.customer.ui.shipper;

import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import vn.vuavuive.shared.data.api.OrderApi;
import vn.vuavuive.shared.data.api.ShipperOrderApi;
import vn.vuavuive.shared.util.SessionManager;

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
public final class ShipperOrderDetailActivity_MembersInjector implements MembersInjector<ShipperOrderDetailActivity> {
  private final Provider<ShipperOrderApi> shipperOrderApiProvider;

  private final Provider<OrderApi> orderApiProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  public ShipperOrderDetailActivity_MembersInjector(
      Provider<ShipperOrderApi> shipperOrderApiProvider, Provider<OrderApi> orderApiProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.shipperOrderApiProvider = shipperOrderApiProvider;
    this.orderApiProvider = orderApiProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  public static MembersInjector<ShipperOrderDetailActivity> create(
      Provider<ShipperOrderApi> shipperOrderApiProvider, Provider<OrderApi> orderApiProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new ShipperOrderDetailActivity_MembersInjector(shipperOrderApiProvider, orderApiProvider, sessionManagerProvider);
  }

  @Override
  public void injectMembers(ShipperOrderDetailActivity instance) {
    injectShipperOrderApi(instance, shipperOrderApiProvider.get());
    injectOrderApi(instance, orderApiProvider.get());
    injectSessionManager(instance, sessionManagerProvider.get());
  }

  @InjectedFieldSignature("vn.vuavuive.customer.ui.shipper.ShipperOrderDetailActivity.shipperOrderApi")
  public static void injectShipperOrderApi(ShipperOrderDetailActivity instance,
      ShipperOrderApi shipperOrderApi) {
    instance.shipperOrderApi = shipperOrderApi;
  }

  @InjectedFieldSignature("vn.vuavuive.customer.ui.shipper.ShipperOrderDetailActivity.orderApi")
  public static void injectOrderApi(ShipperOrderDetailActivity instance, OrderApi orderApi) {
    instance.orderApi = orderApi;
  }

  @InjectedFieldSignature("vn.vuavuive.customer.ui.shipper.ShipperOrderDetailActivity.sessionManager")
  public static void injectSessionManager(ShipperOrderDetailActivity instance,
      SessionManager sessionManager) {
    instance.sessionManager = sessionManager;
  }
}
