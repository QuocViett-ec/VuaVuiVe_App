package vn.vuavuive.customer.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import vn.vuavuive.shared.data.local.AppDatabase;
import vn.vuavuive.shared.data.local.CartDao;

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
public final class DatabaseModule_ProvideCartDaoFactory implements Factory<CartDao> {
  private final DatabaseModule module;

  private final Provider<AppDatabase> databaseProvider;

  public DatabaseModule_ProvideCartDaoFactory(DatabaseModule module,
      Provider<AppDatabase> databaseProvider) {
    this.module = module;
    this.databaseProvider = databaseProvider;
  }

  @Override
  public CartDao get() {
    return provideCartDao(module, databaseProvider.get());
  }

  public static DatabaseModule_ProvideCartDaoFactory create(DatabaseModule module,
      Provider<AppDatabase> databaseProvider) {
    return new DatabaseModule_ProvideCartDaoFactory(module, databaseProvider);
  }

  public static CartDao provideCartDao(DatabaseModule instance, AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(instance.provideCartDao(database));
  }
}
