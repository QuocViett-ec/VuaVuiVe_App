package vn.vuavuive.customer.viewmodel;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import vn.vuavuive.shared.data.api.RecipeApi;

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
public final class RecipeViewModel_Factory implements Factory<RecipeViewModel> {
  private final Provider<RecipeApi> recipeApiProvider;

  public RecipeViewModel_Factory(Provider<RecipeApi> recipeApiProvider) {
    this.recipeApiProvider = recipeApiProvider;
  }

  @Override
  public RecipeViewModel get() {
    return newInstance(recipeApiProvider.get());
  }

  public static RecipeViewModel_Factory create(Provider<RecipeApi> recipeApiProvider) {
    return new RecipeViewModel_Factory(recipeApiProvider);
  }

  public static RecipeViewModel newInstance(RecipeApi recipeApi) {
    return new RecipeViewModel(recipeApi);
  }
}
