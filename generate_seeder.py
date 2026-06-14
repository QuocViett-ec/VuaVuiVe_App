import json
import uuid

recipes_data = [
    {"name": "Canh bí đỏ", "category": "Món canh", "ingredients": ["Bí đỏ", "Thịt heo ba rọi", "Hành lá", "Nước mắm"]},
    {"name": "Cá kho tộ", "category": "Món mặn", "ingredients": ["Cá basa phi lê", "Nước mắm", "Bột ngọt"]},
    {"name": "Rau muống xào tỏi", "category": "Xào, luộc", "ingredients": ["Rau muống", "Dầu ăn", "Muối i-ốt"]},
    {"name": "Canh chua cá basa", "category": "Món canh", "ingredients": ["Cá basa phi lê", "Cà chua bi", "Ngò gai", "Muối", "Nước mắm"]},
    {"name": "Canh rau mồng tơi", "category": "Món canh", "ingredients": ["Rau mồng tơi", "Hành lá", "Muối i-ốt"]},
    {"name": "Khoai tây xào thịt heo", "category": "Xào, luộc", "ingredients": ["Khoai tây", "Thịt heo ba rọi", "Hành lá", "Dầu ăn", "Muối"]},
    {"name": "Cà rốt xào nấm rơm", "category": "Xào, luộc", "ingredients": ["Cà rốt", "Nấm rơm", "Dầu ăn", "Muối"]},
    {"name": "Cải ngọt xào", "category": "Xào, luộc", "ingredients": ["Cải ngọt", "Nước tương", "Dầu ăn", "Muối"]},
    {"name": "Canh bí xanh thịt heo", "category": "Món canh", "ingredients": ["Bí xanh", "Thịt heo ba rọi", "Hành lá", "Muối"]},
    {"name": "Ức gà áp chảo", "category": "Món mặn", "ingredients": ["Ức gà phi lê", "Muối", "Dầu ăn"]},
    {"name": "Nấm kim châm xào ức gà", "category": "Xào, luộc", "ingredients": ["Nấm kim châm", "Ức gà phi lê", "Dầu ăn", "Muối"]},
    {"name": "Đùi gà chiên nước mắm", "category": "Món mặn", "ingredients": ["Đùi gà ta", "Nước mắm", "Dầu ăn"]},
    {"name": "Cá hồi áp chảo", "category": "Món mặn", "ingredients": ["Cá hồi cắt lát", "Muối", "Dầu ăn"]},
    {"name": "Cá basa chiên giòn", "category": "Món mặn", "ingredients": ["Cá basa phi lê", "Bột bánh rán", "Dầu ăn", "Muối"]},
    {"name": "Bắp bò hầm cà rốt", "category": "Món canh", "ingredients": ["Bắp bò", "Cà rốt", "Hành lá", "Muối"]},
    {"name": "Nạm bò xào cải ngồng", "category": "Xào, luộc", "ingredients": ["Nạm bò", "Cải ngồng", "Dầu ăn", "Muối"]},
    {"name": "Cải thìa xào nấm", "category": "Xào, luộc", "ingredients": ["Cải thìa", "Nấm rơm", "Dầu ăn", "Muối"]},
    {"name": "Mực xào hành hẹ", "category": "Xào, luộc", "ingredients": ["Râu mực", "Hành lá", "Hẹ lá", "Dầu ăn", "Muối"]},
    {"name": "Salad xà lách cà chua", "category": "Món chay", "ingredients": ["Xà lách thuỷ canh", "Cà chua bi", "Dầu ăn", "Muối"]},
    {"name": "Bún thịt heo trộn mắm", "category": "Món mặn", "ingredients": ["Bún khô", "Thịt heo ba rọi", "Nước mắm", "Hành lá"]},
    {"name": "Mì xào rau củ", "category": "Món chay", "ingredients": ["Mì Hảo Hảo", "Cà rốt", "Rau muống", "Dầu ăn", "Muối"]},
    {"name": "Cơm rong biển đơn giản", "category": "Món chay", "ingredients": ["Gạo ST25", "Rong biển rắc giòn", "Muối"]},
    {"name": "Canh khổ qua nhồi thịt", "category": "Món canh", "ingredients": ["Khổ qua sơ chế", "Thịt heo ba rọi", "Hành lá", "Nước mắm", "Muối"]},
    {"name": "Gà nướng mật ong", "category": "Món mặn", "ingredients": ["Đùi gà ta", "Muối", "Dầu ăn"]},
    {"name": "Bạch tuộc hấp sả", "category": "Món mặn", "ingredients": ["Bạch tuộc", "Hành lá", "Muối", "Nước mắm"]},
    {"name": "Cơm chiên trứng", "category": "Món mặn", "ingredients": ["Gạo ST25", "Hành lá", "Muối", "Dầu ăn"]},
    {"name": "Nấm kim châm hấp", "category": "Xào, luộc", "ingredients": ["Nấm kim châm", "Hành lá", "Muối", "Dầu ăn"]},
    {"name": "Mực chiên nước mắm", "category": "Món mặn", "ingredients": ["Râu mực", "Nước mắm", "Muối", "Dầu ăn"]},
    {"name": "Canh măng chua cá basa", "category": "Món canh", "ingredients": ["Cá basa phi lê", "Hành lá", "Muối", "Nước mắm"]},
    {"name": "Cải thìa xào thịt bò", "category": "Xào, luộc", "ingredients": ["Cải thìa", "Nạm bò", "Dầu ăn", "Muối"]},
    {"name": "Bắp bò kho tiêu", "category": "Món mặn", "ingredients": ["Bắp bò", "Hành lá", "Muối", "Nước mắm", "Dầu ăn"]},
    {"name": "Thịt heo kho gừng", "category": "Món mặn", "ingredients": ["Thịt heo ba rọi", "Nước mắm", "Muối", "Hành lá"]},
    {"name": "Cá hồi sốt tiêu đen", "category": "Món mặn", "ingredients": ["Cá hồi cắt lát", "Muối", "Dầu ăn"]},
    {"name": "Ức gà sốt chua ngọt", "category": "Món mặn", "ingredients": ["Ức gà phi lê", "Nước tương", "Muối", "Dầu ăn"]},
    {"name": "Salad bầu trộn giấm", "category": "Món chay", "ingredients": ["Bầu sao", "Dầu ăn", "Muối"]},
    {"name": "Bí xanh hấp", "category": "Xào, luộc", "ingredients": ["Bí xanh", "Hành lá", "Muối", "Dầu ăn"]},
    {"name": "Cải bẹ xanh luộc", "category": "Xào, luộc", "ingredients": ["Cải bẹ xanh", "Muối"]},
    {"name": "Canh rau mồng tơi nấu tôm", "category": "Món canh", "ingredients": ["Rau mồng tơi", "Hành lá", "Muối", "Nước mắm"]},
    {"name": "Cá basa kho nghệ", "category": "Món mặn", "ingredients": ["Cá basa phi lê", "Nước mắm", "Muối", "Dầu ăn"]},
    {"name": "Xà lách trộn dầu giấm", "category": "Món chay", "ingredients": ["Xà lách thuỷ canh", "Dầu ăn", "Muối"]},
    {"name": "Mì xào chay", "category": "Món chay", "ingredients": ["Mì Hảo Hảo", "Cải bẹ xanh", "Dầu ăn", "Muối"]}
]

def generate_java_code():
    code = """package vn.vuavuive.backend.modules.recipe;

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
        if (recipeRepository.count() == 0) {
            seedRecipes();
        }
    }

    private void seedRecipes() throws JsonProcessingException {
        List<Recipe> recipes = new ArrayList<>();
        Recipe r;
"""
    for i, item in enumerate(recipes_data):
        ing_list = []
        for ing in item['ingredients']:
            ing_list.append({"name": ing})
        
        ingredients_json = json.dumps(ing_list, ensure_ascii=False).replace('"', '\\"')
        steps_json = json.dumps(["Chuẩn bị và sơ chế sạch sẽ các nguyên liệu.", "Sơ chế các gia vị đi kèm, cắt thái nguyên liệu vừa ăn.", "Bắt đầu chế biến món ăn trên lửa vừa, nêm nếm gia vị cho vừa miệng.", "Trình bày món ăn ra đĩa, trang trí thêm và thưởng thức khi còn nóng."], ensure_ascii=False).replace('"', '\\"')
        
        # Determine prep/cook time
        prep = "15 phút"
        cook = "20 phút"
        diff = "Dễ"
        if "kho" in item['name'].lower() or "hầm" in item['name'].lower():
            cook = "45 phút"
            diff = "Trung bình"
            
        desc = f"Món {item['name']} thơm ngon, đậm đà hương vị truyền thống. Phù hợp cho những bữa cơm gia đình ấm cúng, cung cấp đầy đủ dinh dưỡng và rất đưa cơm."
        image = "https://images.unsplash.com/photo-1547592180-85f173990554?w=500" # Placeholder image

        code += f"""
        r = new Recipe();
        r.setId("recipe_{str(i).zfill(3)}");
        r.setName("{item['name']}");
        r.setDescription("{desc}");
        r.setCategory("{item['category']}");
        r.setPrepTime("{prep}");
        r.setCookTime("{cook}");
        r.setDifficulty("{diff}");
        r.setImage("{image}");
        r.setIngredients("{ingredients_json}");
        r.setSteps("{steps_json}");
        recipes.add(r);
"""
    
    code += """
        recipeRepository.saveAll(recipes);
        System.out.println("Seeded " + recipes.size() + " recipes to database.");
    }
}
"""
    with open("d:/VuaVuiVe_App/app-backend/src/main/java/vn/vuavuive/backend/modules/recipe/RecipeSeeder.java", "w", encoding="utf-8") as f:
        f.write(code)

if __name__ == "__main__":
    generate_java_code()
    print("RecipeSeeder.java generated successfully!")
