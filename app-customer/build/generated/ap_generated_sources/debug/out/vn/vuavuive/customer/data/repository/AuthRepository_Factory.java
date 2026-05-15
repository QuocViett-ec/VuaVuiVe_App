package vn.vuavuive.customer.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import vn.vuavuive.shared.data.api.AuthApi;
import vn.vuavuive.shared.util.SessionManager;

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
public final class AuthRepository_Factory implements Factory<AuthRepository> {
  private final Provider<AuthApi> authApiProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  public AuthRepository_Factory(Provider<AuthApi> authApiProvider,
      Provider<SessionManager> sessionManagerProvider) {
    this.authApiProvider = authApiProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public AuthRepository get() {
    return newInstance(authApiProvider.get(), sessionManagerProvider.get());
  }

  public static AuthRepository_Factory create(Provider<AuthApi> authApiProvider,
      Provider<SessionManager> sessionManagerProvider) {
    return new AuthRepository_Factory(authApiProvider, sessionManagerProvider);
  }

  public static AuthRepository newInstance(AuthApi authApi, SessionManager sessionManager) {
    return new AuthRepository(authApi, sessionManager);
  }
}
