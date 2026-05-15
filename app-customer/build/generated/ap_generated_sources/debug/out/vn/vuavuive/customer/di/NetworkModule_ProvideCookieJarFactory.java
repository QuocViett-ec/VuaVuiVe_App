package vn.vuavuive.customer.di;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import vn.vuavuive.shared.util.PersistentCookieJar;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class NetworkModule_ProvideCookieJarFactory implements Factory<PersistentCookieJar> {
  private final NetworkModule module;

  private final Provider<Context> contextProvider;

  public NetworkModule_ProvideCookieJarFactory(NetworkModule module,
      Provider<Context> contextProvider) {
    this.module = module;
    this.contextProvider = contextProvider;
  }

  @Override
  public PersistentCookieJar get() {
    return provideCookieJar(module, contextProvider.get());
  }

  public static NetworkModule_ProvideCookieJarFactory create(NetworkModule module,
      Provider<Context> contextProvider) {
    return new NetworkModule_ProvideCookieJarFactory(module, contextProvider);
  }

  public static PersistentCookieJar provideCookieJar(NetworkModule instance, Context context) {
    return Preconditions.checkNotNullFromProvides(instance.provideCookieJar(context));
  }
}
