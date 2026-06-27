package vn.vuavuive.backend.modules.recipe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class RecipeSeeder implements CommandLineRunner {

    @Autowired
    private RecipeRepository recipeRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void run(String... args) throws Exception {
        if (recipeRepository.findAll().isEmpty()) {
            seedRecipes();
        }
    }

    private void seedRecipes() throws JsonProcessingException {
        List<Recipe> recipes = new ArrayList<>();
        Recipe r;

        r = new Recipe();
        r.setId("recipe_000");
        r.setName("Canh bí đỏ");
        r.setDescription("Món Canh bí đỏ thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món canh");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Bí đỏ\"}, {\"name\": \"Thịt heo ba rọi\"}, {\"name\": \"Hành lá\"}, {\"name\": \"Nước mắm\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_001");
        r.setName("Cá kho tộ");
        r.setDescription("Món Cá kho tộ thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món mặn");
        r.setPrepTime("15 phút");
        r.setCookTime("45 phút");
        r.setDifficulty("Trung bình");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Cá basa phi lê\"}, {\"name\": \"Nước mắm\"}, {\"name\": \"Bột ngọt\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_002");
        r.setName("Rau muống xào tỏi");
        r.setDescription("Món Rau muống xào tỏi thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Xào, luộc");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Rau muống\"}, {\"name\": \"Dầu ăn\"}, {\"name\": \"Muối i-ốt\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_003");
        r.setName("Canh chua cá basa");
        r.setDescription("Món Canh chua cá basa thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món canh");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Cá basa phi lê\"}, {\"name\": \"Cà chua bi\"}, {\"name\": \"Ngò gai\"}, {\"name\": \"Muối\"}, {\"name\": \"Nước mắm\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_004");
        r.setName("Canh rau mồng tơi");
        r.setDescription("Món Canh rau mồng tơi thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món canh");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Rau mồng tơi\"}, {\"name\": \"Hành lá\"}, {\"name\": \"Muối i-ốt\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_005");
        r.setName("Khoai tây xào thịt heo");
        r.setDescription("Món Khoai tây xào thịt heo thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Xào, luộc");
        r.setPrepTime("15 phút");
        r.setCookTime("45 phút");
        r.setDifficulty("Trung bình");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Khoai tây\"}, {\"name\": \"Thịt heo ba rọi\"}, {\"name\": \"Hành lá\"}, {\"name\": \"Dầu ăn\"}, {\"name\": \"Muối\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_006");
        r.setName("Cà rốt xào nấm rơm");
        r.setDescription("Món Cà rốt xào nấm rơm thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Xào, luộc");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Cà rốt\"}, {\"name\": \"Nấm rơm\"}, {\"name\": \"Dầu ăn\"}, {\"name\": \"Muối\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_007");
        r.setName("Cải ngọt xào");
        r.setDescription("Món Cải ngọt xào thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Xào, luộc");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Cải ngọt\"}, {\"name\": \"Nước tương\"}, {\"name\": \"Dầu ăn\"}, {\"name\": \"Muối\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_008");
        r.setName("Canh bí xanh thịt heo");
        r.setDescription("Món Canh bí xanh thịt heo thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món canh");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Bí xanh\"}, {\"name\": \"Thịt heo ba rọi\"}, {\"name\": \"Hành lá\"}, {\"name\": \"Muối\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_009");
        r.setName("Ức gà áp chảo");
        r.setDescription("Món Ức gà áp chảo thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món mặn");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Ức gà phi lê\"}, {\"name\": \"Muối\"}, {\"name\": \"Dầu ăn\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_010");
        r.setName("Nấm kim châm xào ức gà");
        r.setDescription("Món Nấm kim châm xào ức gà thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Xào, luộc");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Nấm kim châm\"}, {\"name\": \"Ức gà phi lê\"}, {\"name\": \"Dầu ăn\"}, {\"name\": \"Muối\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_011");
        r.setName("Đùi gà chiên nước mắm");
        r.setDescription("Món Đùi gà chiên nước mắm thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món mặn");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Đùi gà ta\"}, {\"name\": \"Nước mắm\"}, {\"name\": \"Dầu ăn\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_012");
        r.setName("Cá hồi áp chảo");
        r.setDescription("Món Cá hồi áp chảo thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món mặn");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Cá hồi cắt lát\"}, {\"name\": \"Muối\"}, {\"name\": \"Dầu ăn\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_013");
        r.setName("Cá basa chiên giòn");
        r.setDescription("Món Cá basa chiên giòn thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món mặn");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Cá basa phi lê\"}, {\"name\": \"Bột bánh rán\"}, {\"name\": \"Dầu ăn\"}, {\"name\": \"Muối\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_014");
        r.setName("Bắp bò hầm cà rốt");
        r.setDescription("Món Bắp bò hầm cà rốt thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món canh");
        r.setPrepTime("15 phút");
        r.setCookTime("45 phút");
        r.setDifficulty("Trung bình");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Bắp bò\"}, {\"name\": \"Cà rốt\"}, {\"name\": \"Hành lá\"}, {\"name\": \"Muối\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_015");
        r.setName("Nạm bò xào cải ngồng");
        r.setDescription("Món Nạm bò xào cải ngồng thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Xào, luộc");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Nạm bò\"}, {\"name\": \"Cải ngồng\"}, {\"name\": \"Dầu ăn\"}, {\"name\": \"Muối\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_016");
        r.setName("Cải thìa xào nấm");
        r.setDescription("Món Cải thìa xào nấm thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Xào, luộc");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Cải thìa\"}, {\"name\": \"Nấm rơm\"}, {\"name\": \"Dầu ăn\"}, {\"name\": \"Muối\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_017");
        r.setName("Mực xào hành hẹ");
        r.setDescription("Món Mực xào hành hẹ thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Xào, luộc");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Râu mực\"}, {\"name\": \"Hành lá\"}, {\"name\": \"Hẹ lá\"}, {\"name\": \"Dầu ăn\"}, {\"name\": \"Muối\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_018");
        r.setName("Salad xà lách cà chua");
        r.setDescription("Món Salad xà lách cà chua thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món chay");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Xà lách thuỷ canh\"}, {\"name\": \"Cà chua bi\"}, {\"name\": \"Dầu ăn\"}, {\"name\": \"Muối\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_019");
        r.setName("Bún thịt heo trộn mắm");
        r.setDescription("Món Bún thịt heo trộn mắm thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món mặn");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Bún khô\"}, {\"name\": \"Thịt heo ba rọi\"}, {\"name\": \"Nước mắm\"}, {\"name\": \"Hành lá\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_020");
        r.setName("Mì xào rau củ");
        r.setDescription("Món Mì xào rau củ thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món chay");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Mì Hảo Hảo\"}, {\"name\": \"Cà rốt\"}, {\"name\": \"Rau muống\"}, {\"name\": \"Dầu ăn\"}, {\"name\": \"Muối\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_021");
        r.setName("Cơm rong biển đơn giản");
        r.setDescription("Món Cơm rong biển đơn giản thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món chay");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Gạo ST25\"}, {\"name\": \"Rong biển rắc giòn\"}, {\"name\": \"Muối\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_022");
        r.setName("Canh khổ qua nhồi thịt");
        r.setDescription("Món Canh khổ qua nhồi thịt thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món canh");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Khổ qua sơ chế\"}, {\"name\": \"Thịt heo ba rọi\"}, {\"name\": \"Hành lá\"}, {\"name\": \"Nước mắm\"}, {\"name\": \"Muối\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_023");
        r.setName("Gà nướng mật ong");
        r.setDescription("Món Gà nướng mật ong thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món mặn");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Đùi gà ta\"}, {\"name\": \"Muối\"}, {\"name\": \"Dầu ăn\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_024");
        r.setName("Bạch tuộc hấp sả");
        r.setDescription("Món Bạch tuộc hấp sả thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món mặn");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Bạch tuộc\"}, {\"name\": \"Hành lá\"}, {\"name\": \"Muối\"}, {\"name\": \"Nước mắm\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_025");
        r.setName("Cơm chiên trứng");
        r.setDescription("Món Cơm chiên trứng thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món mặn");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Gạo ST25\"}, {\"name\": \"Hành lá\"}, {\"name\": \"Muối\"}, {\"name\": \"Dầu ăn\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_026");
        r.setName("Nấm kim châm hấp");
        r.setDescription("Món Nấm kim châm hấp thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Xào, luộc");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Nấm kim châm\"}, {\"name\": \"Hành lá\"}, {\"name\": \"Muối\"}, {\"name\": \"Dầu ăn\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_027");
        r.setName("Mực chiên nước mắm");
        r.setDescription("Món Mực chiên nước mắm thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món mặn");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Râu mực\"}, {\"name\": \"Nước mắm\"}, {\"name\": \"Muối\"}, {\"name\": \"Dầu ăn\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_028");
        r.setName("Canh măng chua cá basa");
        r.setDescription("Món Canh măng chua cá basa thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món canh");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Cá basa phi lê\"}, {\"name\": \"Hành lá\"}, {\"name\": \"Muối\"}, {\"name\": \"Nước mắm\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_029");
        r.setName("Cải thìa xào thịt bò");
        r.setDescription("Món Cải thìa xào thịt bò thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Xào, luộc");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Cải thìa\"}, {\"name\": \"Nạm bò\"}, {\"name\": \"Dầu ăn\"}, {\"name\": \"Muối\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_030");
        r.setName("Bắp bò kho tiêu");
        r.setDescription("Món Bắp bò kho tiêu thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món mặn");
        r.setPrepTime("15 phút");
        r.setCookTime("45 phút");
        r.setDifficulty("Trung bình");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Bắp bò\"}, {\"name\": \"Hành lá\"}, {\"name\": \"Muối\"}, {\"name\": \"Nước mắm\"}, {\"name\": \"Dầu ăn\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_031");
        r.setName("Thịt heo kho gừng");
        r.setDescription("Món Thịt heo kho gừng thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món mặn");
        r.setPrepTime("15 phút");
        r.setCookTime("45 phút");
        r.setDifficulty("Trung bình");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Thịt heo ba rọi\"}, {\"name\": \"Nước mắm\"}, {\"name\": \"Muối\"}, {\"name\": \"Hành lá\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_032");
        r.setName("Cá hồi sốt tiêu đen");
        r.setDescription("Món Cá hồi sốt tiêu đen thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món mặn");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Cá hồi cắt lát\"}, {\"name\": \"Muối\"}, {\"name\": \"Dầu ăn\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_033");
        r.setName("Ức gà sốt chua ngọt");
        r.setDescription("Món Ức gà sốt chua ngọt thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món mặn");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Ức gà phi lê\"}, {\"name\": \"Nước tương\"}, {\"name\": \"Muối\"}, {\"name\": \"Dầu ăn\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_034");
        r.setName("Salad bầu trộn giấm");
        r.setDescription("Món Salad bầu trộn giấm thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món chay");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Bầu sao\"}, {\"name\": \"Dầu ăn\"}, {\"name\": \"Muối\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_035");
        r.setName("Bí xanh hấp");
        r.setDescription("Món Bí xanh hấp thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Xào, luộc");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Bí xanh\"}, {\"name\": \"Hành lá\"}, {\"name\": \"Muối\"}, {\"name\": \"Dầu ăn\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_036");
        r.setName("Cải bẹ xanh luộc");
        r.setDescription("Món Cải bẹ xanh luộc thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Xào, luộc");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Cải bẹ xanh\"}, {\"name\": \"Muối\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_037");
        r.setName("Canh rau mồng tơi nấu tôm");
        r.setDescription("Món Canh rau mồng tơi nấu tôm thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món canh");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Rau mồng tơi\"}, {\"name\": \"Hành lá\"}, {\"name\": \"Muối\"}, {\"name\": \"Nước mắm\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_038");
        r.setName("Cá basa kho nghệ");
        r.setDescription("Món Cá basa kho nghệ thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món mặn");
        r.setPrepTime("15 phút");
        r.setCookTime("45 phút");
        r.setDifficulty("Trung bình");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Cá basa phi lê\"}, {\"name\": \"Nước mắm\"}, {\"name\": \"Muối\"}, {\"name\": \"Dầu ăn\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_039");
        r.setName("Xà lách trộn dầu giấm");
        r.setDescription("Món Xà lách trộn dầu giấm thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món chay");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Xà lách thuỷ canh\"}, {\"name\": \"Dầu ăn\"}, {\"name\": \"Muối\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        r = new Recipe();
        r.setId("recipe_040");
        r.setName("Mì xào chay");
        r.setDescription("Món Mì xào chay thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm.");
        r.setCategory("Món chay");
        r.setPrepTime("15 phút");
        r.setCookTime("20 phút");
        r.setDifficulty("Dễ");
        r.setImage("https://images.unsplash.com/photo-1547592180-85f173990554?w=500");
        r.setIngredients("[{\"name\": \"Mì Hảo Hảo\"}, {\"name\": \"Cải bẹ xanh\"}, {\"name\": \"Dầu ăn\"}, {\"name\": \"Muối\"}]");
        r.setSteps("[\"Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.\", \"Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.\", \"Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.\", \"Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng.\"]");
        recipes.add(r);

        for (Recipe recipe : recipes) {
            recipeRepository.save(recipe);
        }
        System.out.println("Seeded " + recipes.size() + " recipes to database.");
    }
}
