package vn.vuavuive.admin.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;
import vn.vuavuive.shared.util.PersistentCookieJar;

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
public final class NetworkModule_ProvideOkHttpClientFactory implements Factory<OkHttpClient> {
  private final NetworkModule module;

  private final Provider<PersistentCookieJar> cookieJarProvider;

  public NetworkModule_ProvideOkHttpClientFactory(NetworkModule module,
      Provider<PersistentCookieJar> cookieJarProvider) {
    this.module = module;
    this.cookieJarProvider = cookieJarProvider;
  }

  @Override
  public OkHttpClient get() {
    return provideOkHttpClient(module, cookieJarProvider.get());
  }

  public static NetworkModule_ProvideOkHttpClientFactory create(NetworkModule module,
      Provider<PersistentCookieJar> cookieJarProvider) {
    return new NetworkModule_ProvideOkHttpClientFactory(module, cookieJarProvider);
  }

  public static OkHttpClient provideOkHttpClient(NetworkModule instance,
      PersistentCookieJar cookieJar) {
    return Preconditions.checkNotNullFromProvides(instance.provideOkHttpClient(cookieJar));
  }
}
