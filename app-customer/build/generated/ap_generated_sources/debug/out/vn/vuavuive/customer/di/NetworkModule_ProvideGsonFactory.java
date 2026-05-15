package vn.vuavuive.customer.di;

import com.google.gson.Gson;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class NetworkModule_ProvideGsonFactory implements Factory<Gson> {
  private final NetworkModule module;

  public NetworkModule_ProvideGsonFactory(NetworkModule module) {
    this.module = module;
  }

  @Override
  public Gson get() {
    return provideGson(module);
  }

  public static NetworkModule_ProvideGsonFactory create(NetworkModule module) {
    return new NetworkModule_ProvideGsonFactory(module);
  }

  public static Gson provideGson(NetworkModule instance) {
    return Preconditions.checkNotNullFromProvides(instance.provideGson());
  }
}
