package vn.vuavuive.backend.modules.recipe;

import java.util.UUID;

/**
 * Lớp Recipe — Thông tin công thức nấu ăn, loại bỏ JPA.
 */
public class Recipe {

    private String id;
    private String name;
    private String description;
    private String image;
    private String category;
    private String prepTime;
    private String cookTime;
    private String difficulty;
    private Object ingredients;
    private Object steps;

    public Recipe() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPrepTime() { return prepTime; }
    public void setPrepTime(String prepTime) { this.prepTime = prepTime; }

    public String getCookTime() { return cookTime; }
    public void setCookTime(String cookTime) { this.cookTime = cookTime; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Object getIngredients() { return ingredients; }
    public void setIngredients(Object ingredients) { this.ingredients = ingredients; }
 
    public Object getSteps() { return steps; }
    public void setSteps(Object steps) { this.steps = steps; }
}
