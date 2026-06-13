package vn.vuavuive.customer.ui.auth;

import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
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
public final class LoginActivity_MembersInjector implements MembersInjector<LoginActivity> {
  private final Provider<SessionManager> sessionManagerProvider;

  public LoginActivity_MembersInjector(Provider<SessionManager> sessionManagerProvider) {
    this.sessionManagerProvider = sessionManagerProvider;
  }

  public static MembersInjector<LoginActivity> create(
      Provider<SessionManager> sessionManagerProvider) {
    return new LoginActivity_MembersInjector(sessionManagerProvider);
  }

  @Override
  public void injectMembers(LoginActivity instance) {
    injectSessionManager(instance, sessionManagerProvider.get());
  }

  @InjectedFieldSignature("vn.vuavuive.customer.ui.auth.LoginActivity.sessionManager")
  public static void injectSessionManager(LoginActivity instance, SessionManager sessionManager) {
    instance.sessionManager = sessionManager;
  }
}
