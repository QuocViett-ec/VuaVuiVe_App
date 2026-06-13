package vn.vuavuive.customer.ui.shipper;

import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
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
public final class ShipperMainActivity_MembersInjector implements MembersInjector<ShipperMainActivity> {
  private final Provider<SessionManager> sessionManagerProvider;

  private final Provider<ShipperOrderApi> shipperOrderApiProvider;

  public ShipperMainActivity_MembersInjector(Provider<SessionManager> sessionManagerProvider,
      Provider<ShipperOrderApi> shipperOrderApiProvider) {
    this.sessionManagerProvider = sessionManagerProvider;
    this.shipperOrderApiProvider = shipperOrderApiProvider;
  }

  public static MembersInjector<ShipperMainActivity> create(
      Provider<SessionManager> sessionManagerProvider,
      Provider<ShipperOrderApi> shipperOrderApiProvider) {
    return new ShipperMainActivity_MembersInjector(sessionManagerProvider, shipperOrderApiProvider);
  }

  @Override
  public void injectMembers(ShipperMainActivity instance) {
    injectSessionManager(instance, sessionManagerProvider.get());
    injectShipperOrderApi(instance, shipperOrderApiProvider.get());
  }

  @InjectedFieldSignature("vn.vuavuive.customer.ui.shipper.ShipperMainActivity.sessionManager")
  public static void injectSessionManager(ShipperMainActivity instance,
      SessionManager sessionManager) {
    instance.sessionManager = sessionManager;
  }

  @InjectedFieldSignature("vn.vuavuive.customer.ui.shipper.ShipperMainActivity.shipperOrderApi")
  public static void injectShipperOrderApi(ShipperMainActivity instance,
      ShipperOrderApi shipperOrderApi) {
    instance.shipperOrderApi = shipperOrderApi;
  }
}
