package vn.vuavuive.backend.modules.recipe;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.vuavuive.backend.core.FirebaseRepositoryHelper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RecipeRepository {

    private final FirebaseRepositoryHelper firebase;

    public Optional<Recipe> findById(String id) {
        return Optional.ofNullable(firebase.get("recipes/" + id, Recipe.class));
    }

    public List<Recipe> findAll() {
        return firebase.getList("recipes", Recipe.class);
    }

    public Recipe save(Recipe recipe) {
        if (recipe.getId() == null) {
            recipe.setId(UUID.randomUUID().toString());
        }
        firebase.save("recipes/" + recipe.getId(), recipe);
        return recipe;
    }

    public void deleteById(String id) {
        firebase.delete("recipes/" + id);
    }
}
