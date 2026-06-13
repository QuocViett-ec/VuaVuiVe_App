package vn.vuavuive.backend.modules.recipe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import vn.vuavuive.backend.core.ApiResponse;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recipes")
@CrossOrigin(origins = "*")
public class RecipeController {

    @Autowired
    private RecipeRepository recipeRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getRecipes() {
        List<Recipe> recipes = recipeRepository.findAll();
        List<Map<String, Object>> result = recipes.stream()
                .map(this::convertRecipeToMap)
                .collect(Collectors.toList());
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getRecipe(@PathVariable String id) {
        Optional<Recipe> recipeOpt = recipeRepository.findById(id);
        if (recipeOpt.isPresent()) {
            return ApiResponse.success(convertRecipeToMap(recipeOpt.get()));
        }
        return ApiResponse.error("Không tìm thấy công thức");
    }

    private Map<String, Object> convertRecipeToMap(Recipe recipe) {
        Map<String, Object> map = new HashMap<>();
        map.put("_id", recipe.getId());
        map.put("name", recipe.getName());
        map.put("description", recipe.getDescription());
        map.put("image", recipe.getImage());
        map.put("category", recipe.getCategory());
        map.put("prepTime", recipe.getPrepTime());
        map.put("cookTime", recipe.getCookTime());
        map.put("difficulty", recipe.getDifficulty());

        try {
            if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
                List<Map<String, Object>> ingredients = objectMapper.readValue(recipe.getIngredients(), new TypeReference<List<Map<String, Object>>>() {});
                map.put("ingredients", ingredients);
            } else {
                map.put("ingredients", new ArrayList<>());
            }

            if (recipe.getSteps() != null && !recipe.getSteps().isEmpty()) {
                List<String> steps = objectMapper.readValue(recipe.getSteps(), new TypeReference<List<String>>() {});
                map.put("steps", steps);
            } else {
                map.put("steps", new ArrayList<>());
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return map;
    }
}
