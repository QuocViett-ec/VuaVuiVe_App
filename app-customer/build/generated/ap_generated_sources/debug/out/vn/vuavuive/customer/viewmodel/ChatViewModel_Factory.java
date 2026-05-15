package vn.vuavuive.customer.viewmodel;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import vn.vuavuive.shared.data.api.ChatbotApi;

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
public final class ChatViewModel_Factory implements Factory<ChatViewModel> {
  private final Provider<ChatbotApi> chatbotApiProvider;

  public ChatViewModel_Factory(Provider<ChatbotApi> chatbotApiProvider) {
    this.chatbotApiProvider = chatbotApiProvider;
  }

  @Override
  public ChatViewModel get() {
    return newInstance(chatbotApiProvider.get());
  }

  public static ChatViewModel_Factory create(Provider<ChatbotApi> chatbotApiProvider) {
    return new ChatViewModel_Factory(chatbotApiProvider);
  }

  public static ChatViewModel newInstance(ChatbotApi chatbotApi) {
    return new ChatViewModel(chatbotApi);
  }
}
